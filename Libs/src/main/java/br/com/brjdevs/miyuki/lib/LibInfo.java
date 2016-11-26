package br.com.brjdevs.miyuki.lib;

public class LibInfo {
	public static final String VERSION_MAJOR = "@libVersionMajor@";
	public static final String VERSION_MINOR = "@libVersionMinor@";
	public static final String VERSION_REVISION = "@libVersionRevision@";
	public static final String VERSION_BUILD = "@libVersionBuild@";
	public static final String VERSION = VERSION_MAJOR.startsWith("@") ? "dev" : String.format("%s.%s.%s_%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD);
}
