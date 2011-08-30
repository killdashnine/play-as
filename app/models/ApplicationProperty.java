/*
 * Copyright 2011 Matthias van der Vlies
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * JPA entity used for specifying a configuration property for an application 
 */
@Entity
@Table(name="application_properties")
public class ApplicationProperty extends Model {

	/**
	 * The application this property is for
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	public Application application;
	
	/**
	 * Configuration key
	 */
	@Column(length = 100)
	public String key;
	
	/**
	 * Configuration value
	 */
	@Column(length = 200)
	public String value;
	
	/**
	 * Priority (used for ordering lines in the exported file)
	 */
	public Integer priority;
	
	public ApplicationProperty() {
		// no arg constructor
	}
	
	public ApplicationProperty(final Application application, final Integer priority, final String key, final String value) {
		this.key = key;
		this.value = value;
		this.application = application;
		this.priority = priority;
	}
	
	@Transient
	public static ApplicationProperty findLogFileProperty(final Application application) {
		return (ApplicationProperty) find("application = ? and key = ?", application, "log4j.appender.Rolling.File").first();
	}
}
