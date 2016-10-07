/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.NlpCriticContext;
import org.openmrs.module.ncd.database.NlpCriticContextGroup;
import org.openmrs.module.ncd.database.NlpCriticContextType;

/**
 *
 */
public interface INlpCriticContextDAO {
    public List<NlpCriticContext> findContextByType(NlpCriticContextType type);
    public List<NlpCriticContext> findContextByTypeAndGroup(NlpCriticContextType type, String group);
    public List<NlpCriticContextType> listContextTypes();
    public NlpCriticContextType findContextTypeByName(String name);
    public void saveNlpCriticContextType(NlpCriticContextType type);
    public void deleteNlpCriticContextType(NlpCriticContextType type);
    public void saveNlpCriticContext(NlpCriticContext context);
    public void deleteNlpCriticContext(NlpCriticContext context);
    public List<NlpCriticContextGroup> listContextGroups();
}
