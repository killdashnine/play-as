package scm;

public interface VersionControlSystem {

	public String checkout(final String pid, final String url) throws Exception;
	public String update(final String pid) throws Exception;
	public String cleanup(final String pid) throws Exception;
}
