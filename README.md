# DeadByCraft
A recreation of Dead by Daylight in Minecraft.

# Compiling

### Maven
Maven requires setting up profiles and defining the token else where https://help.github.com/en/github/managing-packages-with-github-packages/configuring-apache-maven-for-use-with-github-packages

### Gradle
In your `build.gradle` add: 
```groovy
repositories {
	maven {
		url 'https://maven.pkg.github.com/Sitrica/DeadByCraft/'
		credentials {
			username = "Sitrica"
			password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_PACKAGES_KEY")
		}
	}
}

dependencies {
	compile (group: 'com.sitrica', name: 'DeadByCraft', version: '1.0.0')
}
```
Getting a Github token:

1.) Go into your account settings on Github and create a personal token with the read:packages scope checked.

2.) Generate that key, and now go add a System Environment Variable named GITHUB_PACKAGES_KEY

3.) Restart computer or if using Chocolatey type `refreshenv`

Note: you can just directly put your token as the password, but we highly discourage that.