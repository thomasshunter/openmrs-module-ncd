/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.CriticDef;

public interface ICriticDAO {
    public CriticDef findCriticById(Long id);
    public List<CriticDef> getAllCritics();
}
