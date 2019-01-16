package com.quant.challenge.checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * This class represents a path to be approved encapsulating the logic to load owners and dependencies per folder.
 */
public class ApprovalPath {

    public static final String OWNERS_FILENAME = "OWNERS";
    public static final String DEPENDENCIES_FILENAME = "DEPENDENCIES";

    private Path systemRoot, path;
    private Set<String> owners = new HashSet<>();
    private Set<Path> dependencies = new HashSet<>();

    /**
     * Constructor
     * @param systemRoot root's path
     * @param path path to be checked (directory), relative to {@code systemRoot}
     * @throws ApprovalException is an exception is found while walking through paths
     */
    public ApprovalPath(Path systemRoot, Path path) throws ApprovalException {
        this.systemRoot = systemRoot.normalize();
        this.path = path.normalize();
        loadOwners();
        loadDependencies();
    }

    /**
     * @return set of all owners of this path, it is all owners in it and parents as well
     */
    public Set<String> getOwners() {
        return owners;
    }

    /**
     * @return set of all dependencies for this path
     */
    public Set<Path> getDependencies() {
        return dependencies;
    }

    /**
     *  Utility method for loading all owners of this path including root's OWNERS file.
     * @throws ApprovalException if an exception is found
     */
    protected void loadOwners() throws ApprovalException {
        Path currentPath = systemRoot.resolve(path).normalize();
        do {
            Path ownersFilePath = currentPath.resolve(OWNERS_FILENAME);
            if(Files.exists(ownersFilePath)) {
                try (Scanner scanner = new Scanner(ownersFilePath)) {
                    while (scanner.hasNextLine()) {
                        owners.add(scanner.nextLine());
                    }
                } catch (IOException e) {
                    throw new ApprovalException("Error while getting owners for path: " + currentPath, e);
                }
            }
            if (!systemRoot.equals(currentPath)) // for allowing loading OWNERS from root path, eg: this.path == ""
                currentPath = currentPath.getParent();
        } while(!systemRoot.equals(currentPath) && currentPath != null);
    }

    /**
     *  Utility method for loading dependencies of this path.
     * @throws ApprovalException if an exception is found
     */
    protected void loadDependencies() throws ApprovalException {
        Path dependenciesFilePath = systemRoot.resolve(path).resolve(DEPENDENCIES_FILENAME);
        if(Files.exists(dependenciesFilePath)) {
            try (Scanner scanner = new Scanner(dependenciesFilePath)) {
                while (scanner.hasNextLine()) {
                    dependencies.add(Paths.get(scanner.nextLine()).normalize());
                }
            } catch (IOException e) {
                throw new ApprovalException("Error while getting dependencies for path: " + path, e);
            }
        }
    }
}
