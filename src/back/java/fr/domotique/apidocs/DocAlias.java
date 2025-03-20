package fr.domotique.apidocs;

import java.lang.annotation.*;

/// A little hack to copy the API schema of a class, under a different name.
///
/// Usually, this is used to circumvent Java's limitation on generic types.
///
/// Basically, if you want document a type of `MyType<Integer>`, you'll need to create a class extending
/// from `DocAlias<MyType<Integer>>`.
///
/// ## Example
///
/// ```java
/// // Create a RegistrationError schema that's just an ErrorResponse<ErrData>
/// class RegistrationError extends DocAlias<ErrorResponse<ErrData>> {
///     static final ErrorResponse<ErrData> EXAMPLE = new ErrorResponse<>("Something went wrong!", "REGISTRATION_ERR", new ErrData("bla bla"));
/// }
/// ```
public abstract class DocAlias<T> {
}
