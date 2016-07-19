Open a terminal.
Use "cd" command to navigate to this directory.

Type the following command:

./run.sh


# to generate a key
ssh-keygen -t rsa


# profile script:
```
#!/bin/sh
cd ~/log-archiver/ && ./run.sh username@server.com
chmod 775 .bin/serverlogs
```

# add to ~/.bash_profile:
export PATH=$PATH:~/.bin/

