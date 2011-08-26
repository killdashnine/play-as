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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.Play.Mode;
import play.db.jpa.Model;

@Entity
@Table(name="application_properties")
public class ApplicationProperty extends Model {

	@ManyToOne(fetch=FetchType.LAZY)
	public Application application;
	
	public String key;
	public String value;
	
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
}
