package fr.domotique.base;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/// Contains errors for each attribute of a request. Is able to end a request when there is any error.
///
/// ## Attribute-error association
///
/// Each attribute name is associated with either:
///
/// | Type | Description | Method |
/// |------|-------------|--------|
/// | `List<String>` | For numbers, strings and booleans | [#addError(String, String)] |
/// | `ValidationBlock` | For child objects and maps | [#child(String)] |
/// | `ValidationBlock[]` | For child lists | [#childArray(String, int)] |
///
/// (Note that this is not true internally for performance and practicality reasons)
///
/// ## Root errors
///
/// Validation blocks may contain a special attribute, named `$root`, containing errors involving the entire request.
///
/// To add errors to the root, use the [#addRootError(String)] method.
///
/// ## Root/child blocks
///
/// Validation blocks exist in two states:
/// - **Root**: they contain the entire request's errors and can end the request.
/// - **Child**: they contain errors for a specific attribute and can be nested in root blocks.
///
/// A **child** block may not know if it's invalid; only a **root** block can do so.
///
/// ## Ending blocks
///
/// You have two options to start and end blocks:
/// - **Manually**: you can start a block with [Validation#start())] and end it with [#end()].
///   ```java
///   ValidationBlock block = Validation.start();
///   block.addError("name", "The name is invalid!");
///   block.end();
///   ```
/// - **Automatically**: you can use a block as a resource in a try-with-resources statement.
///   ```java
///   try (ValidationBlock block = Validation.start()) {
///      block.addError("name", "The name is invalid!");
///   }
///   ```
///
/// My recommendation to decide between the two is:
/// - Use the **automatic** syntax when possible;
/// - Use the **manual** syntax when you must (i.e. if you need to do asynchronous requests)
///
/// ## Example
/// ```java
/// ValidationBlock block = Validation.start();
///
/// // Adding errors manually
/// if (name.isBlank()) {
///     block.addError("name", "The name is invalid!");
/// }
///
/// // Adding errors using the Validation class
/// Validation.nonBlank(block, "description", description, "The description is invalid!");
///
/// // Report any errors we've found. If there's none, the program will continue.
/// block.end();
///```
///
/// ## Possible output
///
/// ```json
/// {
///    "name": ["The name is invalid!"],
///    "description": ["The description is invalid!"]
/// }
///```
///
/// @author Dynamic
/// @see Validation
public final class ValidationBlock implements AutoCloseable {
    /// error is either:
    /// - `HashMap<String, Object>[]` from a `ValidationBlock[]` --- child array
    /// - `HashMap<String, Object>` from a `ValidationBlock` --- child object/child map
    /// - `List<String>` --- property errors (leaf)
    private final HashMap<String, Object> errors = new HashMap<>(); // Possible optimisation: don't create until first error

    /// The root validation block used to turn on the "invalid" boolean.
    ///
    /// Is set to self when this is the root block.
    private final @NotNull ValidationBlock root;

    /// True when this block (or any of its descendants) has AT LEAST one error
    /// This is only taken in consideration for root blocks.
    private boolean invalid = false;

    /// Create a new validation block with the given root.
    ///
    /// A null root will set the root to itself.
    private ValidationBlock(@Nullable ValidationBlock root) {
        if (root == null) {
            this.root = this;
        } else {
            this.root = root;
        }
    }

    /// Create a new validation block with no root.
    public ValidationBlock() {
        this(null);
    }

    public static final String ROOT_KEY = "$root";

    /// Puts a child validation block for attribute `key`.
    ///
    /// Empty blocks will lead to an empty JSON object: `{}`.
    ///
    /// Replaces any previous entry.
    ///
    /// ## Example
    ///
    /// ```java
    /// ValidationBlock block = Validation.start();
    ///
    /// ValidationBlock userBlock = block.child("user");
    /// userBlock.addError("name", "The name is invalid!");
    ///
    /// ValidationBlock deviceBlock = block.child("device");
    /// deviceBlock.addError("temperature", "The temperature is way too hot!");
    ///
    /// block.end();
    ///```
    ///
    /// ## Output
    ///
    /// ```json
    ///{
    ///    "user": {
    ///       "name": ["The name is invalid!"]
    ///     },
    ///    "device": {
    ///       "temperature": ["The temperature is way too hot!"]
    ///     }
    ///}
    ///```
    ///
    /// @return the new validation block
    public ValidationBlock child(String key) {
        // Create the validation block and add it to the map.
        ValidationBlock block = new ValidationBlock(this);
        errors.put(key, block.errors);
        return block;
    }

    /// Puts an array of validation blocks of size `size` for attribute `key`.
    ///
    /// In practice, you'd use the other [#childArray(String, Collection, BiConsumer)] function though.
    ///
    /// Empty blocks will lead to an empty JSON object: `{}`.
    ///
    /// Replaces any previous entry.
    ///
    /// ## Example
    ///
    /// ```java
    /// ValidationBlock block = Validation.start();
    ///
    /// // Create child array for validating a list of users
    /// ValidationBlock[] userBlocks = block.childArray("users", 3);
    /// userBlocks[0].addError("name", "First user name is invalid!");
    /// userBlocks[1].addError("email", "Second user email is invalid!");
    /// // Nothing for the third user!
    ///
    /// block.end();
    ///```
    ///
    /// ## Output
    ///
    /// ```json
    ///{
    ///    "users": [
    ///      {
    ///          "name": ["First user name is invalid!"]
    ///      },
    ///      {
    ///          "email": ["Second user email is invalid!"]
    ///      },
    ///      {}
    ///   ]
    ///}
    ///```
    ///
    /// @param key the attribute name
    /// @param size the size of the array
    /// @return an array of validation blocks
    @SuppressWarnings("unchecked")
    public ValidationBlock[] childArray(String key, int size) {
        // Check stuff
        Objects.requireNonNull(key, "The key must not be null!");
        if (size < 0) {
            throw new IllegalArgumentException("Size must be positive!");
        }

        var blocks = new ValidationBlock[size];
        var blockErrs = (HashMap<String, Object>[]) new HashMap[size];
        for (int i = 0; i < size; i++) {
            var block = new ValidationBlock(this);
            blocks[i] = block;
            blockErrs[i] = blocks[i].errors;
        }
        errors.put(key, blockErrs);
        return blocks;
    }

    /// Puts an array of validation blocks from the `values` collection, using the `validator`
    /// to create a block for each value.
    ///
    /// Empty blocks will lead to an empty JSON object: `{}`.
    ///
    /// ## Example
    ///
    /// ```java
    /// ValidationBlock block = Validation.start();
    ///
    /// List<User> users = getUsers();
    /// block.childArray("users", users, (user, b) -> {
    ///     Validation.nonBlank(b, "name", user.name, "Name is required!");
    ///     Validation.nonBlank(b, "email", user.email, "Email is required!");
    ///});
    ///
    /// block.end();
    ///```
    ///
    /// ## Output
    ///
    /// ```json
    ///{
    ///    "users": [
    ///        {
    ///          "name": ["Name is required!"]
    ///        },
    ///        {
    ///          "email": ["Email is required!"]
    ///        }
    ///    ]
    ///}
    ///```
    ///
    /// @param key the attribute name
    /// @param values the collection of values to validate
    /// @param validator function that validates each value and adds errors to its block
    /// @return an array of validation blocks
    public <T> ValidationBlock[] childArray(String key,
                                            Collection<T> values,
                                            BiConsumer<? super T, ValidationBlock> validator) {
        // Create the array of blocks using the collection's size.
        var blocks = childArray(key, values.size());

        // We need to count manually the index of a collection.
        int i = 0;
        for (T val : values) {
            validator.accept(val, blocks[i]);
            i++;
        }
        return blocks;
    }

    // todo: child map maybe?

    /// Adds an error to the block, with the attribute name `key`.
    ///
    /// Fails if the attribute already has a [child block][#child(String)] or a [child array][#childArray(String, int)].
    ///
    /// ## Example
    ///
    /// ```java
    /// ValidationBlock block = Validation.start();
    ///```
    @SuppressWarnings("unchecked")
    public void addError(String key, String err) {
        Object entry = errors.computeIfAbsent(key, _ -> new ArrayList<String>());
        if (entry instanceof List<?> l) {
            ((List<String>) l).add(err);

            // Mark the root block as invalid (remember that the root block's root... is itself!)
            root.invalid = true;
        } else {
            throw new IllegalStateException("Key " + key + " is not a list of errors! (found " + entry.getClass() + ")");
        }
    }

    /// Adds an error to the root part of the block.
    ///
    /// The root part is, in JSON, the string `"$root"`.
    ///
    /// ## Example
    /// ```java
    /// ValidationBlock block = Validation.start();
    /// block.addRootError("The data is invalid!");
    /// block.end();
    ///```
    ///
    /// ## Output
    /// ```json
    /// {
    ///   "$root": ["The data is invalid!"]
    /// }
    ///```
    public void addRootError(String err) {
        addError(ROOT_KEY, err);
    }

    /// Throws a [RequestException] if there's any error in the block. Only valid for root blocks.
    ///
    /// The exception will contain (in the `data` attribute) all errors, which will be sent in JSON format.
    ///
    /// @throws IllegalStateException if this is not a root block
    public void end() {
        if (root != this) {
            throw new IllegalStateException("Cannot end a child block.");
        }

        if (invalid) {
            throw new RequestException("Les données entrées sont invalides.", 422, "VALIDATION_FAILED", errors);
        }
    }

    /// Returns `true` when this root block has at least one error, including in its descendants.
    ///
    /// @throws IllegalStateException if this is not a root block
    public boolean isInvalid() {
        if (root != this) {
            throw new IllegalStateException("A child block has no invalid status.");
        }

        return invalid;
    }

    /// Returns the errors in the block, in a map format which can be serialized to JSON easily.
    public HashMap<String, Object> getErrors() {
        return errors;
    }

    @Override
    public void close() {
        end();
    }
}
