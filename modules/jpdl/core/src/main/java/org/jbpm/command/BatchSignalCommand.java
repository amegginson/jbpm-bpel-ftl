package org.jbpm.command;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.Token;

/**
 * a bunch of processes is signalled with this command. you can specify the
 * tokens either
 * <li> by a array of token ids
 * <li> or by processName, processVersion (optional, without all versions),
 * stateName
 * 
 * transitionName speicifies the transition to take (if null, the default
 * transition is taken).
 * 
 * This command can be for example useful, if you have a lot of processes to
 * check some information a not jBPM task has altered before (maybe in batch
 * too).
 * 
 * CURRENTLY EXPERIMENTAL! 
 * 
 * @author Bernd Rucker (bernd.ruecker@camunda.com)
 */
public class BatchSignalCommand implements Command {

    private static final long serialVersionUID = -4330623193546102772L;

    private static Log log = LogFactory.getLog(BatchSignalCommand.class);

    private long[] tokenIds = null;

    private String processName = null;

    private String stateName = null;
    
    /**
     * if set, only tokens which are started after this date are signaled
     * (interessting to implement some timeout for example)
     */
    private Date inStateAtLeastSince = null;

    private long processVersion = 0;

    private String transitionName = null;

    public Object execute(JbpmContext jbpmContext) throws Exception {
	log.debug("executing " + this);

	// batch tokens
	if (tokenIds != null && tokenIds.length > 0) {
	    for (int i = 0; i < tokenIds.length; i++) {
	        Token token = jbpmContext.loadTokenForUpdate(tokenIds[i]);
	        signalToken(token);
	    }
	}

	// search for tokens in process/state
	if (processName != null && stateName != null) {
	    Query query = jbpmContext
		    .getSession()
		    .getNamedQuery(
			    "GraphSession.findTokensForProcessInNode");
	    query.setString("processDefinitionName", processName);
        query.setString("nodeName", stateName);

        Iterator iter = query.list().iterator();
        while (iter.hasNext()) {
            Token t = (Token) iter.next();
            if (inStateAtLeastSince== null || t.getNodeEnter().before(inStateAtLeastSince))
              signalToken(t);            
        }
	}

	return null;
    }

    private void signalToken(Token token) {
        log.debug("signal token " + token);
        if (transitionName == null) {
            token.signal();
        }
        else {
            token.signal(transitionName);
        }
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public long getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(long processVersion) {
        this.processVersion = processVersion;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public long[] getTokenIds() {
        return tokenIds;
    }

    public void setTokenIds(long[] tokenIds) {
        this.tokenIds = tokenIds;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    public Date getInStateAtLeastSince() {
      return inStateAtLeastSince;
    }

    public void setInStateAtLeastSince(Date inStateAtLeastSince) {
      this.inStateAtLeastSince = inStateAtLeastSince;
    }

}
