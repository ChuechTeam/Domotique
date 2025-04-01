/// The foundation of our web application.
///
/// Contains all necessary utilities for all requests, stuff like:
/// - [validation][fr.domotique.base.ValidationBlock]: making sure the user input is correct
/// - [sanitization][fr.domotique.base.Sanitize]: preventing absurd string inputs from entering the database
/// - [error handling][fr.domotique.base.RequestException]: managing errors and exceptions
/// - [API documentation][fr.domotique.base.apidocs.DocsGen]: creating OpenAPI documentation for our endpoints
/// - [database access][fr.domotique.base.data.Table]: facilitating access to the database with SQL queries
///
/// Usually, as a backend API developer, you shouldn't have to edit code in this package too much. (Except
/// maybe for [fr.domotique.base.Validation] and [fr.domotique.base.Sanitize] to add common features)
///
/// @author Dynamic
package fr.domotique.base;