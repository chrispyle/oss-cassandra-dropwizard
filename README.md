![Logo](https://www.clearcapital.com/wp-content/uploads/2015/02/Clear-Capital@2x.png)
# Cassandra/DropWizard

## What is it?

A little glue between
[oss-cassandra-helpers](http://github.com/clearcapital/oss-cassandra-helpers/README.md) and
DropWizard.

Specifically, we provide here a predefined command for doing schema
comparisons. To use it, add this to your `*Service.initialize(bootstrap<{ConfigurationType}> bootstrap)`
method:

```java
bootstrap.addCommand(new AutoSchemaCommand<{ConfigurationType}>({ConfigurationType}.class));
```

And then run your service with `auto-schema` instead of with `service`.


