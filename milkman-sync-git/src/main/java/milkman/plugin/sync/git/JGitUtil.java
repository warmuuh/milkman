package milkman.plugin.sync.git;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.DefaultProxyDataFactory;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;

import lombok.RequiredArgsConstructor;

public class JGitUtil {

	public static<R extends TransportCommand<C, T>, C extends GitCommand<T>, T> R initWith(R cmd, GitSyncDetails sync){
		
		if (sync.isSsh()) {
			cmd.setTransportConfigCallback(new SshTransportConfigCallback(sync.getUsername(), sync.getPasswordOrToken()));
			if (StringUtils.isNotBlank(sync.getPasswordOrToken())) {
				cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(sync.getUsername(), sync.getPasswordOrToken()));
			}

		} else {
			cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(sync.getUsername(), sync.getPasswordOrToken()));
		}
		
		return cmd;
	}
	
	
	@RequiredArgsConstructor
	private static class SshTransportConfigCallback implements TransportConfigCallback {

		private final String pathToKey;
		private final String passphrase;
		
//	    private final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
//
//	        @Override
//	        protected void configure(OpenSshConfig.Host hc, Session session) {
////	            session.setConfig("StrictHostKeyChecking", "no");
//	        }
//
//	        @Override
//	        protected JSch createDefaultJSch(FS fs) throws JSchException {
//	            JSch jSch = super.createDefaultJSch(fs);
//	            
//	            if (StringUtils.isNotBlank(passphrase)) {
//					jSch.addIdentity(pathToKey, passphrase.getBytes());
//				} else {
//					jSch.addIdentity(pathToKey);
//				}
//	            
//	            return jSch;
//	        }
//	    };
		
		static {
			SshdSessionFactory factory = new SshdSessionFactory(
					new JGitKeyCache(), new DefaultProxyDataFactory());
			Runtime.getRuntime()
					.addShutdownHook(new Thread(() -> factory.close()));
			SshSessionFactory.setInstance(factory);
		}
		

	    @Override
	    public void configure(Transport transport) {
	        SshTransport sshTransport = (SshTransport) transport;
	        sshTransport.setSshSessionFactory(SshdSessionFactory.getInstance());
	    }
	}
}
