package org.openmrs.module.ncd.model;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/** Demographic information about a provider
 * @author Erik Horstkotte 
 */
public class ProviderInfo implements PersonAddressInfo, PersonNameInfo 
{
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffixName;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String county;
    private String phoneNumber;
    private String facilityId;
    private String localId;
    private String localIdSource;
    private String nameSource;
    private Date birthDate;
    private String deaNumber;
    private String fax;
    private String license;
    private String nameMatched;
    private String practice;
    private String ssn;
    
    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        
        out.append( "\n-------------------------" );
        out.append( "\n       ProviderInfo      " );
        out.append( "\n-------------------------" );
        out.append( "\n firstName              =" + this.firstName );
        out.append( "\n middleName             =" + this.middleName );
        out.append( "\n lastName               =" + this.lastName );
        out.append( "\n suffixName             =" + this.suffixName );
        out.append( "\n street1                =" + this.street1 );
        out.append( "\n street2                =" + this.street2 );
        out.append( "\n city                   =" + this.city );
        out.append( "\n state                  =" + this.state );
        out.append( "\n zip                    =" + this.zip );
        out.append( "\n county                 =" + this.county );
        out.append( "\n phoneNumber            =" + this.phoneNumber );
        out.append( "\n facilityId             =" + this.facilityId );
        out.append( "\n localId                =" + this.localId );
        out.append( "\n localIdSource          =" + this.localIdSource );
        out.append( "\n nameSource             =" + this.nameSource );
        out.append( "\n birthDate              =" + this.birthDate );
        out.append( "\n deaNumber              =" + this.deaNumber );
        out.append( "\n fax                    =" + this.fax );
        out.append( "\n license                =" + this.license );
        out.append( "\n nameMatched            =" + this.nameMatched );
        out.append( "\n practice               =" + this.practice );
        out.append( "\n ssn                    =" + this.ssn );
        out.append( "\n-------------------------" );
        
        return out.toString();
    }

    @Override
    public String getCountry() 
    {
        return null; // Not currently extracted.
    }

    public Date getBirthDate() 
    {
        return birthDate;
    }
    public void setBirthDate(Date birthDate) 
    {
        this.birthDate = birthDate;
    }
    
    public String getDeaNumber() 
    {
        return deaNumber;
    }
    public void setDeaNumber(String deaNumber) 
    {
        this.deaNumber = deaNumber;
    }
    
    public String getFax() 
    {
        return fax;
    }
    public void setFax(String fax) 
    {
        this.fax = fax;
    }
    
    public String getLicense() 
    {
        return license;
    }
    public void setLicense(String license) 
    {
        this.license = license;
    }
    
    public String getNameMatched() 
    {
        return nameMatched;
    }
    public void setNameMatched(String nameMatched) 
    {
        this.nameMatched = nameMatched;
    }
    
    public String getPractice() 
    {
        return practice;
    }
    public void setPractice(String practice) 
    {
        this.practice = practice;
    }
    
    public String getSSN() 
    {
        return ssn;
    }
    public void setSSN(String ssn) 
    {
        this.ssn = ssn;
    }
    
    public String getCity() 
    {
        return city;
    }
    public void setCity(String city) 
    {
        this.city = city;
    }
    
    public String getCounty() 
    {
        return county;
    }
    public void setCounty(String county) 
    {
        this.county = county;
    }
    
    public String getPhoneNumber() 
    {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) 
    {
        this.phoneNumber = phoneNumber;
    }
    
    public String getState() 
    {
        return state;
    }
    public void setState(String state) 
    {
        this.state = state;
    }
    
    public String getStreet1() 
    {
        return street1;
    }
    public void setStreet1(String street1) 
    {
        this.street1 = street1;
    }
    
    public String getStreet2() 
    {
        return street2;
    }
    public void setStreet2(String street2) 
    {
        this.street2 = street2;
    }
    
    public String getZip() 
    {
        return zip;
    }
    public void setZip(String zip) 
    {
        this.zip = zip;
    }
    
    public String getFacilityId() 
    {
        return facilityId;
    }
    public void setFacilityId(String facilityId) 
    {
        this.facilityId = facilityId;
    }
    
    public String getLocalId() 
    {
        return localId;
    }
    public void setLocalId(String localId) 
    {
        this.localId = localId;
    }
    
    public String getLocalIdSource() 
    {
        return localIdSource;
    }
    public void setLocalIdSource(String localIdSource) 
    {
        this.localIdSource = localIdSource;
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#getFirstName()
     */
    public String getFirstName() 
    {
        return firstName;
    }
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#setFirstName(java.lang.String)
     */
    public void setFirstName(String firstName) 
    {
        this.firstName = firstName;
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#getMiddleName()
     */
    public String getMiddleName() 
    {
        return middleName;
    }
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#setMiddleName(java.lang.String)
     */
    public void setMiddleName(String middleName) 
    {
        this.middleName = middleName;
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#getLastName()
     */
    public String getLastName() 
    {
        return lastName;
    }
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#setLastName(java.lang.String)
     */
    public void setLastName(String lastName) 
    {
        this.lastName = lastName;
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#getFullName()
     */
    public String getFullName() 
    {
        String firstName    = getFirstName();
        String middleName   = getMiddleName();
        String lastName     = getLastName();
        String suffixName   = getSuffixName();
        String fullName     = "";
    
        if (StringUtils.isNotEmpty(lastName) || StringUtils.isNotEmpty(firstName) || StringUtils.isNotEmpty(middleName))
        {
            fullName = lastName + ", " + firstName + " " + middleName + " " + suffixName;
            fullName = fullName.trim();
        }
        
        return fullName;
    }
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#getSuffixName()
     */
    public String getSuffixName() 
    {
        return suffixName;
    }
    /**
     * @see org.openmrs.module.ncd.model.PersonNameInfo#setSuffixName(java.lang.String)
     */
    public void setSuffixName(String suffixName) 
    {
        this.suffixName = suffixName;
    }
    
    public String getNameSource() 
    {
        return nameSource;
    }
    public void setNameSource(String nameSource) 
    {
        this.nameSource = nameSource;
    }
    
}
