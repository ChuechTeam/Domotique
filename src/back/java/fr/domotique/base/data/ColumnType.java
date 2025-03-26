package fr.domotique.base.data;

/// A type of column for [EntityColumn]: key or not key.
public enum ColumnType {
    /// A usual column, with no default value or particular constraints
    NORMAL(true, true, false),
    /// A `PRIMARY KEY` that needs to be set manually in `INSERT``
    MANUAL_KEY(true, false, true),
    /// A `PRIMARY KEY` with `AUTO_INCREMENT` turned on; does not figure in `INSERT`
    GENERATED_KEY(false, false, true);

    /// true when this column should be included in INSERT queries: `INSERT INTO Table (col1, col2)``
    final boolean insertArg;
    /// True when this column should be included in UPDATE queries: `UPDATE Table SET col1 = ?, col2 = ?``
    final boolean updateArg;
    /// True when this column is a `PRIMARY KEY`.
    final boolean key;

    ColumnType(boolean insertArg, boolean updateArg, boolean key) {
        this.insertArg = insertArg;
        this.updateArg = updateArg;
        this.key = key;
    }
}
