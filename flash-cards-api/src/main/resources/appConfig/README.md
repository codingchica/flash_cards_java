# Application Configuration Files
While it is possible to maintain different configurations for different individual environments here, there is also a cost to doing so.
Keeping the various files in sync may be a bit painful as your configuration grows. 

It may be worth looking into a programmatic approach to determining environment variables, and then setting those up to be consumed by the more-generic application configuration files here.
Some examples:
- Ansible
- Helm Charts
- Terraform
- etc.

Doing so would allow logic for individual fields to be specified for different environments, all in one location, while here, the logic would be scattered among different files.

## Configuration Validation
[Dropwizard Validations](https://www.dropwizard.io/en/latest/manual/validation.html) also apply to the configuration files.

## Runtime POJO
The plain old java object (POJO) used to represent the configurations in this directory can be found at [DropwizardTestDemoConfiguration.java](../../main/java/codingchica/demo/test/dropwizard/config/DropwizardTestDemoConfiguration.java).
When extending the built-in Dropwizard configuration, remember to add validations, too.
Also, don't forget `NotNull` and `Valid` annotations, when appropriate.  They are easily overlooked.

## Unit Test Validation
Given valid, known environment variables, the unit tests will enforce that the configuration in the files in this directory are valid.
See [ConfigurationFileValidationTest.java](../../test/java/codingchica/demo/test/dropwizard/core/config/ConfigurationFileValidationTest.java)

## Related Folders
- src/test/resources/appConfig - test configurations.

## Reference Materials
- [Dropwizard Configuration Reference](https://www.dropwizard.io/en/latest/manual/configuration.html)
  - [Polymorphic configuration](https://www.dropwizard.io/en/latest/manual/configuration.html#polymorphic-configuration)
- [Dropwizard Environment Variables](https://dropwizardio.readthedocs.io/en/latest/manual/core.html#environment-variables)
- [Hibernate Validator](https://hibernate.org/validator/)
  - [Built-In Constraints](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-builtin-constraints)
  - [Custom Constraints](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#validator-customconstraints)
