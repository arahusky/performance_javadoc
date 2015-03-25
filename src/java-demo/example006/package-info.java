/*
 Copyright 2014 Jakub Naplava
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * <p>
 * This package contains code to show the effectivity of different approaches to
 * inserting multiple records into database.</p>
 *
 * <p>
 * Note that because we're working with Derby <b>embedded</b> database, only one
 * VM can have access to the database, thus no code generation with measuring
 * code in another VM is possible (<b>codeGenerationFlag in
 * measurement.properties must be set to false</b>).</p>
 */
package example006;