# Building from Source

## Building with Maven

### Step 1: Setup Github credentials
Create a `settings.xml` file with your GitHub username and personal authentication token. This is required to download dependencies from the GitHub Package Manager.

!!!info Token Permissions
Use [classic](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic) token with `read:packages` permissions.
!!!

```xml settings.xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR-GITHUB-USERNAME</username>
            <password>YOUR-GITHUB-PAT</password>
        </server>
    </servers>
</settings>
```

### Step 2: Install
To compile the cBioPortal source code, move into the root directory and run the following maven command. Remember to pass your `settings.xml` that you created in the previous step:

```
mvn --settings settings.xml -DskipTests clean install
```

Note: cBioPortal 6.X requires Java 21
