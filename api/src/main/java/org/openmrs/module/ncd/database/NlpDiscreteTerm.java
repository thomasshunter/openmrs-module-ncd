/**
 * 
 */
package org.openmrs.module.ncd.database;

/**
 * @author jlbrown
 *
 */
public class NlpDiscreteTerm implements java.io.Serializable {

	private static final long serialVersionUID = 1200248183910515474L;
	private Long id;
	private String term;
	private boolean negative;
	
	public NlpDiscreteTerm() {
		this.term = null;
		this.negative = false;
	}
	
	public NlpDiscreteTerm(String term, boolean negative) {
		this.term = term;
		this.negative = negative;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public boolean isNegative() {
		return negative;
	}
	
	public void setNegative(boolean negative) {
		this.negative = negative;
	}	
	
	public String toString() {
		
		return "NlpDiscreteTerm(" +
					"id=" + id +
					", term=" + term +
					", negative=" + negative +
			   ")";
	}
}
