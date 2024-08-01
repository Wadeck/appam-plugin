# Appam plugin

## Introduction

This plugin is only meant for security training. It should not be put used in any production system.
It voluntarily contains several vulnerabilities.

## Getting started

To run directly the plugin: `mvn hpi:run`.

Otherwise, you can compile is using `mvn package` and then install it on a running Jenkins instance.

## Name history
This repository is the 2024 edition of the sister repository: https://github.com/Wadeck/emmenthal-plugin.

As this is used as a workshop in India, the name was adjusted to something in the indian cuisine that has holes.

The [appam](https://en.wikipedia.org/wiki/Appam) is a type of thin pancake originating from South India and Sri Lanka.
It is made with fermented rice batter and coconut milk.

![Appam](docs/images/appam.jpg)

## Topics

### Level 1: Missing permission
Reference: https://owasp.org/Top10/A01_2021-Broken_Access_Control/
Jenkins documentation: https://www.jenkins.io/doc/developer/security/#what-do-plugins-need-to-do-to-protect-web-methods
- missing
- incorrect
- wrong scope

### Level 2: Cross-site Request Forgery (CSRF)
Reference: https://owasp.org/www-community/attacks/csrf

### Level 3: Cross-site Scripting (XSS)
Reference: https://owasp.org/www-community/attacks/xss/
Jenkins documentation: https://www.jenkins.io/doc/developer/security/xss-prevention/
- stored
- reflected

### Level 4: XML External Entity (XXE)
Reference: https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing
- SSRF
- secrets theft

### Level 5: Path Traversal
Reference: https://owasp.org/www-community/attacks/Path_Traversal
- arbitrary file read

### Level 6: Improper Secret Storage
Reference: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
Jenkins documentation: https://www.jenkins.io/doc/developer/security/secrets/
- plain text (any)
- hashed (password)
- encrypted (token)
- timing attack

### Level 7: Deny of Service (DoS)
Reference: https://owasp.org/www-community/attacks/Denial_of_Service
- recursive calls
- regex based

### Level 8: Open Redirect
Reference: https://cheatsheetseries.owasp.org/cheatsheets/Unvalidated_Redirects_and_Forwards_Cheat_Sheet.html

### Level 9: Incorrect Usage of Credentials
Jenkins documentation: https://github.com/jenkinsci/credentials-plugin/blob/master/docs/consumer.adoc
- incorrect scope (system vs global)
- credentials enumeration
- credentials theft

