package scm;

public class VersionControlSystemFactory {

	public enum VersionControlSystemType {
		GIT
	}
	
	public static VersionControlSystem getVersionControlSystem(final VersionControlSystemType type) throws Exception {
		switch(type) {
			case GIT:
				return new GitVersionControlSystem();
			default:
				throw new Exception("Unimplemented VCS");
		}
	}
}
