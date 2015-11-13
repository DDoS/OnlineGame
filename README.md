# Building
- Install Gradle and the plugin for your IDE if needed.  
- Use `gradle build` from the command line.

# Running
- Use `gradle run -PappArgs="['--mode', 'client']"`. You
  can change the argument list to whatever you need. Other
  arguments include `--ip` and `--port`.

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
