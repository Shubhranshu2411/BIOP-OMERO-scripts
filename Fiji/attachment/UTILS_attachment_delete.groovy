#@String(label="Username") USERNAME
#@String(label="Password", style='password' , value=PASSWORD , persist=false) PASSWORD
#@Long(label="ID", value=119273) id
#@String(label="Object", choices={"image","dataset","project","well","plate","screen"}) object_type


/* 
 * == INPUTS ==
 *  - credentials 
 *  - id
 *  - object type
 * 
 * == OUTPUTS ==
 *  - deletion of the attachment on OMERO
 * 
 * = DEPENDENCIES =
 *  - Fiji update site OMERO 5.5-5.6
 *  - simple-omero-client-5.9.1 : https://github.com/GReD-Clermont/simple-omero-client
 * 
 * = INSTALLATION = 
 *  Open Script and Run
 * 
 * = AUTHOR INFORMATION =
 * Code written by Rémy Dornier, EPFL - SV -PTECH - BIOP 
 * 01.09.2022
 * 
 * = COPYRIGHT =
 * © All rights reserved. ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland, BioImaging And Optics Platform (BIOP), 2022
 * 
 * Licensed under the BSD-3-Clause License:
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided 
 * that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
 *    in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * == HISTORY ==
 * - 2023-06-16 : Limits the number of call to the OMERO server
 */

/**
 * Main. Connect to OMERO, delete attachments and disconnect from OMERO
 * 
 */
 
// Connection to server
host = "omero-server.epfl.ch"
port = 4064

Client user_client = new Client()
user_client.connect(host, port, USERNAME, PASSWORD.toCharArray())

if (user_client.isConnected()){
	println "\nConnected to "+host
	
	try{
		switch (object_type){
			case "image":	
				processAttachment(user_client, user_client.getImage(id))
				break	
			case "dataset":
				processAttachment(user_client, user_client.getDataset(id))
				break
			case "project":
				processAttachment(user_client, user_client.getProject(id))
				break
			case "well":
				processAttachment(user_client, user_client.getWells(id))
				break
			case "plate":
				processAttachment(user_client, user_client.getPlates(id))
				break
			case "screen":
				processAttachment(user_client, user_client.getScreens(id))
				break
		}
		println "Processing of attachments for "+object_type+ " "+id+" : DONE !"
		
	} finally {
		user_client.disconnect()
		println "Disonnected from "+host
	}
	
} else {
	println "Not able to connect to "+host
}


/**
 * Delete all the attachment from an object
 * BE CAREFUL : you will delete the attachment, not remove the attachment from the object. Meaning that every people that use this attachment will losse it.
 * 
 * inputs
 * 		user_client : OMERO client
 * 		repository_wpr : OMERO repository object (image, dataset, project, well, plate, screen)
 * 
 * */
def processAttachment(user_client, repository_wpr){
	
	def file_wpr_list = repository_wpr.getFileAnnotations(user_client)
	List<FileAnnotationWrapper> attachments_to_delete = []
	
	def userId = user_client.getUser().getId()
	def ownerRepoId = repository_wpr.getOwner().getId()
	
	if (ownerRepoId == userId){
		file_wpr_list.each{file_wpr->
			if  (file_wpr.getOwner().getId() == userId){
					println file_wpr.getFileName() + " will be deleted"
					attachments_to_delete.add(file_wpr)
			} else {
				println file_wpr.getFileName() + " will NOT be deleted"
			}
		}
	} else {
		println file_wpr.getName() + " will NOT be deleted"
	}
	
	if(!attachments_to_delete.isEmpty())
		user_client.delete((Collection<GenericObjectWrapper<?>>)attachments_to_delete)
		
	println attachments_to_delete.size() + " attachments deleted"
}


/*
 * imports  
 */
import fr.igred.omero.*
import fr.igred.omero.repository.*
import fr.igred.omero.annotations.*
