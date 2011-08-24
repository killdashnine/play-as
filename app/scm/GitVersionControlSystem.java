package scm;

public class GitVersionControlSystem implements VersionControlSystem {

	public String checkout(final String pid, final String gitUrl) throws Exception {
		final String checkoutPid = "git-checkout-" + pid;
		return ScmUtils.executeScmProcess(checkoutPid, "git clone " + gitUrl + " apps/" + pid);
	}
	
	public String update(final String pid) throws Exception {
		final String checkoutPid = "git-pull-" + pid;
		return ScmUtils.executeScmProcess(checkoutPid, "git --git-dir=apps/" + pid + "/.git --work-tree=apps/" + pid + " pull origin master");
	}
	
	public String cleanup(final String pid) throws Exception {
		final String checkoutPid = "git-checkout-" + pid;
		return ScmUtils.executeScmProcess(checkoutPid, "git --git-dir=apps/" + pid + "/.git --work-tree=apps/" + pid + " checkout -- conf/application.conf");
	}
}
