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
register the private key in the Jenkins server with the name 
'GITHUB_MACHINE_USER_PRIVATE_KEY'. Finally, you register the public 
key in your GitHubu machine user's account and be sure that user has
write access to your target repository.
