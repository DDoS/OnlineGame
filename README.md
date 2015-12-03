# Building
- Install Gradle and the plugin for your IDE if needed.  
- Use `gradle build` from the command line.

# Running
- Use `gradle run -PappArgs="['--mode', 'server']"`. You
  can change the argument list to whatever you need.
- You can also get the jar from the build folder
  
## Arguments
- `--mode`: either `server` or `client`, defaults to server
- `--type`: either `udp` or `tcp`, defaults to UDP
- `--ip`: the IP address of the server to connect to when in client mode
- `--port`: the port to bind to in server mode or to connect to in client mode
- `--headless`: don't run the GUI in client mode

Example to start a server: `--mode server`
Example to connect to the server on the same machine: `--mode client --ip localhost`

# Guidelines
## Formatting
- LF line endings only, you can enforce this in your git settings.  
- Indentation is 4 spaces, *not* tabs.  
- Format your code to match the style of the existing code,
  use your IDE's autoformatter to your advantage.  
- Document all non-trivial public fields, methods and classes.

## Committing
- Split changes into separate commits when it makes sense.
- Use descriptive commit messages.
- Don't be afraid to use branches for larger features.
- **Always test your code, never push anything broken to master**.
  It's fine if your pushing to a branch though.
- When pulling use `git pull -r` or when merging `git rebase`.
  Rebase is easier and cleaner than a merge.
- Merge WIP commits with `git rebase` or amend the latest one using
  `git commit -a --amend`, if it hasn't already been pushed.
- If you screw up a commit, you can use force push, but do it quick
  because if someone pulls the broken version, it will mess up his
  history.
