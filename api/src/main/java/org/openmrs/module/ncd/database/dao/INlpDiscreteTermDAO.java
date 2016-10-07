package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.NlpDiscreteTerm;

public interface INlpDiscreteTermDAO {
	public List<NlpDiscreteTerm> getNlpDiscreteTermsByNegative(boolean isNegative);
	public List<NlpDiscreteTerm> getAllNlpDiscreteTerms();
	public void saveNlpDiscreteTerm(NlpDiscreteTerm term);
	public void deleteNlpDiscreteTerm(NlpDiscreteTerm term);
}
