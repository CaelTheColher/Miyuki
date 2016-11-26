package br.com.brjdevs.miyuki.framework;

public class FrameworkInfo {
	public static final String VERSION_MAJOR = "@frameworkVersionMajor@";
	public static final String VERSION_MINOR = "@frameworkVersionMinor@";
	public static final String VERSION_REVISION = "@frameworkVersionRevision@";
	public static final String VERSION_BUILD = "@frameworkVersionBuild@";
	public static final String VERSION = VERSION_MAJOR.startsWith("@") ? "dev" : String.format("%s.%s.%s_%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD);
}
