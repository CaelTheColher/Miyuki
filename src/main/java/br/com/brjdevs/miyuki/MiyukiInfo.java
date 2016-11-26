package br.com.brjdevs.miyuki;

public class MiyukiInfo {
	public static final String VERSION_MAJOR = "@rootVersionMajor@";
	public static final String VERSION_MINOR = "@rootVersionMinor@";
	public static final String VERSION_REVISION = "@rootVersionRevision@";
	public static final String VERSION_BUILD = "@rootVersionBuild@";
	public static final String VERSION = VERSION_MAJOR.startsWith("@") ? "dev" : String.format("%s.%s.%s_%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD);
}
