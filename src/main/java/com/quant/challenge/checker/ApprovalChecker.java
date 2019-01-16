package com.quant.challenge.checker;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for handling the approval process across the whole tree.
 * This class is not thread-safe.
 */
public class ApprovalChecker {

    private Path systemRoot;

    private Set<String> approvers;

    private Set<Path> changedFiles;

    /**
     * Constructor for Approval check process.
     * @param systemRoot system's root path
     * @param approvers list of approvals
     * @param changedFiles list of changed files
     */
    public ApprovalChecker(Path systemRoot, Set<String> approvers, Set<Path> changedFiles) {
        this.systemRoot = systemRoot;
        this.approvers = approvers;
        this.changedFiles = changedFiles;
    }

    /**
     * Main method for checking if all changes are approved.
     * @return {@link ApprovalResult}
     * @throws ApprovalException if an exception found while checking ({@link java.io.IOException} or wrong paths)
     */
    public ApprovalResult check() throws ApprovalException {

        // Optimization: if any owners from the system root path are present, then all are approved
        ApprovalPath systemRootApprovalPath = new ApprovalPath(systemRoot, Paths.get(""));
        if (systemRootApprovalPath.getOwners().stream().filter(o -> approvers.contains(o)).findAny().isPresent())
            return ApprovalResult.APPROVED; // fast early approval, root's owners approve everything in cascade

        // Iterates the whole list of changed files in the commit or changeset
        for (Path changedFile : changedFiles) {
            // Check it is a regular file, symbolic links are treated as files as well in a source versioning system
            Path fullChangedFilePath = systemRoot.resolve(changedFile).normalize();
            if (!Files.isRegularFile(fullChangedFilePath, LinkOption.NOFOLLOW_LINKS))
                throw new ApprovalException("Not a regular file in changed files list: " + fullChangedFilePath, null);
            // Calls the recursive function to see if changed file  is approved with all its dependencies
            if (!isChangedPathApproved(changedFile.getParent(), new HashSet<>()))
                return ApprovalResult.DISAPPROVED; // one file not properly approved
        }
        return ApprovalResult.APPROVED; // all files with their dependencies were approved
    }

    /**
     * Recursive method for evaluating an individual path approval with all its dependencies.
     * @param changedPath the path (directory) to check (parent of changed file or a dependency)
     * @param previousPaths the set of previous paths being evaluated before reaching the current dependency
     *                      to avoid infinite recursion due to cyclic dependencies.
     * @return {@code true} if current path (with dependencies) is approved, {@code false} otherwise
     * @throws ApprovalException if an exception found while checking ({@link java.io.IOException} or wrong paths)
     */
    protected boolean isChangedPathApproved(Path changedPath, Set<Path> previousPaths) throws ApprovalException {
        // builds a path with dependency and owners
        ApprovalPath approvalPath = new ApprovalPath(systemRoot, changedPath);
        // adds the current path to exclusion list to avoid infinite recursion due to cyclic dependencies
        previousPaths.add(changedPath.normalize());

        //for all dependencies, check recursively they are properly approved (excluding the current and previous paths)
        for (Path dependency : approvalPath.getDependencies()) {
            dependency = dependency.normalize();
            if (!previousPaths.contains(dependency) && !isChangedPathApproved(dependency, previousPaths))
                return false; // one not properly approved makes the entire process to fail
        }

        // do not remove the current approved path from previousPaths,
        // so to avoid checking it again from other dependencies

        // final result is true if at least one owner for this changed path is present in the approvers list
        return approvalPath.getOwners().stream().filter(o -> approvers.contains(o)).findAny().isPresent();
    }

}
