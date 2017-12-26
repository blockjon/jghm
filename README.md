# Requirements
The following are required for this library to fully work.

### GitHub API Key Credential

The GitHub.groovy module allows you to send status updates to
GitHub. In order for the authorization to work, you must set 
a credential in your Jenkins credentials keystore called 
"GITHUB_API_KEY" set with a string value of a key on a user's
github account with write permission to the repo. An ideal 
GitHub user would be a machine user.

### SSH Private Key Slaves Can Use For Cloning Your Git Repositories

When the slave machines try to use the git cli to interact with
your GitHub repo, they will need a private key that has access 
to your GitHub repository.

The recommended approach is to create a Jenkins machine user used 
for cloning source code to the Jenkins slaves. After you generate 
the rsa key pair with something like `ssh-keygen -t rsa`, you then 
propagate the private key to each of the Jenkins slaves with the 
script below, and then you register the public key in your Jenkins
machine user's public keys. Finally, be sure you have granted permission
for your machine user to pull and push from your GitHub repository.

The following script can be used to propagate the private key to all
of your Jenkins slaves:

```
FOO
```
