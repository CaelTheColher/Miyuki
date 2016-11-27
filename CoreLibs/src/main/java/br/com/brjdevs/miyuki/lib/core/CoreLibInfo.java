package br.com.brjdevs.miyuki.lib.core;

public class CoreLibInfo {
	public static final String VERSION_MAJOR = "@corelibVersionMajor@";
	public static final String VERSION_MINOR = "@corelibVersionMinor@";
	public static final String VERSION_REVISION = "@corelibVersionRevision@";
	public static final String VERSION_BUILD = "@corelibVersionBuild@";
	public static final String VERSION = VERSION_MAJOR.startsWith("@") ? "dev" : String.format("%s.%s.%s_%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD);
}
