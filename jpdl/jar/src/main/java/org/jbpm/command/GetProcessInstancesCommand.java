package org.jbpm.command;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * This command can retrieve all process instances (e.g. for admin client).
 * 
 * You have the possibility to filter the command, therefor use the available
 * attributes
 * 
 * @author Bernd Ruecker (bernd.ruecker@camunda.com)
 */
public class GetProcessInstancesCommand extends AbstractGetObjectBaseCommand {

    private static final long serialVersionUID = -5601050489405283851L;

    /*
     * is true, only the running process instances are retrieved (ended and
     * canceled ones are skipped)
     */
    private boolean onlyRunning = true;

    /*
     * if given, only processes with start date >= given date are shown
     */
    private Date fromStartDate;

    /*
     * if given, only processes with start date <= given date are shown
     */
    private Date untilStartDate;

    /*
     * if given, only processes with this name are retrieved
     */
    private String processName;

    /*
     * if given, only processes with this name are retrieved
     */
    private String stateName;

    private transient boolean firstExpression = true;

    private String getConcatExpression() {
        if (firstExpression) {
            firstExpression = false;
            return " where ";
        }
        return " and ";
    }

    public Object execute(JbpmContext jbpmContext) throws Exception {
        setJbpmContext(jbpmContext);
        firstExpression = true;
        StringBuffer queryText = new StringBuffer("select pi" + " from org.jbpm.graph.exe.ProcessInstance as pi ");

        if (onlyRunning) {
            queryText.append(getConcatExpression()).append(" pi.end = null");
        }

        if (fromStartDate != null) {
            queryText.append(getConcatExpression()).append(" pi.start >= :from ");
        }
        if (untilStartDate != null) {
            queryText.append(getConcatExpression()).append(" pi.start <= :until ");
        }

        // name
        if (processName != null && processName.length() > 0) {
            queryText.append(getConcatExpression()).append(" pi.processDefinition.name = :processDefinitionName  ");
        }

        // TODO: this code only fecthes root tokens, child-tokens has to be
        // considered too!
        if (stateName != null && stateName.length() > 0) {
            queryText.append(getConcatExpression()).append(" pi.rootToken.node.name = :nodeName ");
        }

        queryText.append(" order by pi.start desc");

        Query query = jbpmContext.getSession().createQuery(queryText.toString());

        if (fromStartDate != null) {
            query.setDate("from", fromStartDate);
        }
        if (untilStartDate != null) {
            query.setDate("until", untilStartDate);
        }

        if (processName != null && processName.length() > 0) {
            query.setString("processDefinitionName", processName);
        }

        if (stateName != null && stateName.length() > 0) {
            query.setString("nodeName", stateName);
        }

        return retrieveProcessInstanceDetails(query.list());
    }

    /**
     * access everything on all processInstance objects, which is not in the
     * default fetch group from hibernate, but needs to be accesible from the
     * client
     * 
     * overwrite this, if you need more details in your client
     */
    public List retrieveProcessInstanceDetails(List processInstanceList) {
        Iterator it = processInstanceList.iterator();
        while (it.hasNext()) {
            retrieveProcessInstance((ProcessInstance) it.next());
        }
        return processInstanceList;
    }

    public Date getFromStartDate() {
        return fromStartDate;
    }

    public void setFromStartDate(Date fromStartDate) {
        this.fromStartDate = fromStartDate;
    }

    public boolean isOnlyRunning() {
        return onlyRunning;
    }

    public void setOnlyRunning(boolean onlyRunning) {
        this.onlyRunning = onlyRunning;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Date getUntilStartDate() {
        return untilStartDate;
    }

    public void setUntilStartDate(Date untilStartDate) {
        this.untilStartDate = untilStartDate;
    }

}
