package dev.zerojdk.adapter.out.unarchiver;

public record ArchiveEntry(Kind kind, String name, String content) {
    enum Kind { FILE, DIRECTORY, SYMLINK, HARDLINK }

    public static ArchiveEntry file(String name, String content) {
        return new ArchiveEntry(Kind.FILE, name, content);
    }

    public static ArchiveEntry directory(String name) {
        if (!name.endsWith("/")) {
            name = name + '/';
        }

        return new ArchiveEntry(Kind.DIRECTORY, name, null);
    }

    public static ArchiveEntry symlink(String name, String linkTarget) {
        return new ArchiveEntry(Kind.SYMLINK, name, linkTarget);
    }

    static ArchiveEntry hardlink(String name, String linkTarget) {
        return new ArchiveEntry(Kind.HARDLINK, name, linkTarget);
    }

    boolean isDirectory() {
        return kind == Kind.DIRECTORY;
    }

    boolean isSymlink() {
        return kind == Kind.SYMLINK;
    }
}
