# Validate Approvals

## Description
This version of the approval system uses two types of files. Each directory in the
repository may contain one or both of them. They contain the information used to identify who
must approve a change. They are DEPENDENCIES and OWNERS.

### DEPENDENCIES files
These files contain a list of paths, one per line. These paths are the directories containing
sources that the current directory's sources depend on. Paths must be relative to the root
directory of the source code repository. If a directory does not contain a DEPENDENCIES file, it
is equivalent to containing an empty DEPENDENCIES file.

### OWNERS files
These files contain a list of usernames of engineers, one per line. The usernames refer to the
engineers who can approve changes affecting the containing directory and its subdirectories. If
there is no OWNERS file or it is empty, then the parent directory's OWNERS file should be
used.

### Approval rules
A change is approved if all of the affected directories are approved.
A directory is considered to be affected by a change if either: (1) a file in that directory was
changed, or (2) a file in a (transitive) dependency directory was changed.
In case (1), we only consider changes to files directly contained within a directory, not files in
subdirectories, etc.
Case (2) includes transitive changes, so a directory is also affected if a dependency of one of its
dependencies changes, etc.
A directory has approval if at least one engineer listed in an OWNERS file in it or any of its
parent directories has approved it.

## Requirements

* JDK 1.8+
* Maven

## Install

1. `unzip validate_approvals.zip`
2. `cd validate_approvals`
3. `mvn install`

## Running

From validate_approvals run:
`java -jar target/validate_approvals-1.0-jar-with-dependencies.jar --changed-files <changed filed> --approvers <approvers> --system-root <system root>`

Argument --system-root is optional, it can be absolute or relative.
Current working directory will be taken if not provided.

## Running Examples

```
java -jar target/validate_approvals-1.0-jar-with-dependencies.jar --changed-files src/com/client/message/Message.java,src/com/client/follow/Follow.java --approvers alovelace,eclarke,user --system-root ../repo_root
Approved
```

```
java -jar target/validate_approvals-1.0-jar-with-dependencies.jar --changed-files src/com/client/message/Message.java,src/com/client/follow/Follow.java --approvers eclarke,user --system-root ../repo_root
Insuficient Approvals
```


