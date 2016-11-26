package br.com.brjdevs.miyuki.modules;

public class ModuleInfo {
	public static final String VERSION_MAJOR = "@modulesVersionMajor@";
	public static final String VERSION_MINOR = "@modulesVersionMinor@";
	public static final String VERSION_REVISION = "@modulesVersionRevision@";
	public static final String VERSION_BUILD = "@modulesVersionBuild@";
	public static final String VERSION = VERSION_MAJOR.startsWith("@") ? "dev" : String.format("%s.%s.%s_%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD);
}
