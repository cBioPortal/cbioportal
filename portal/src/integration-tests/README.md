# Integration tests

This folder contains maven projects that test particular functionality of `cbioportal.war` started under different settings.

These tests get called by `maven-invoker-plugin` that is declared in the `portal` maven module of the project.

Keeping tests as separate maven projects liberate us from the application dependencies and unnecessary conflicts between them and test dependencies.