/*
Copyright 2019 GEOSIRIS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import {sendGetURL, sendGetURL_Promise, sendPostForm_Func} from "../requests/requests.js"
import {createDeleteButton, createDropDownButton, createHoverableHtmlContent, createInputGroup, geosiris_createEditableSelector, getPopoverOpenUUID} from "../UI/htmlUtils.js"
import {openResqmlObjectContentByUUID, saveResqmlObjectByUUID} from "../main.js"
import {generateUUID, isResqmlListType, isUUID} from "../common/utils.js"
import {createSnackBar} from "../UI/snackbar.js"
import {extTypeAttributes, mapResqmlEnumToValues, mapResqmlTypeToSubtypes, mapResqmlTypesComment, openHDFViewAskH5Location} from "./epcContentManager.js"
import {closeModal, openModal} from "../UI/modals/modalEntityManager.js"
import {createTableFilterInput, createTableFromData, transformTabToFormCheckable, transformTabToFormRadio} from "../UI/table.js"
import {UUID_REGEX, UUID_REGEX_str} from "../common/variables.js"
import {geosiris_DndHandler} from "../UI/dragAndDrop.js"


const ResqmlElement_NAMESUBATTPREFIX = "subAtt_";

export function ResqmlElement_isNullAttribute(resqmlElt){
	return 	resqmlElt==null || ((resqmlElt.attributes == null || resqmlElt.attributes.length <= 0)
						&&   resqmlElt.value == null 
						&& 	(resqmlElt.properties == null || resqmlElt.properties.length <= 0));
						
}

export function getHtmlEltIdxInParent(elt_html){
	var chIdx = 0;
	var child = elt_html;
	while( (child = child.previousSibling) != null ) 
		chIdx++;
	return chIdx;
}
export function getResqmlEltCitationTitle(resqmlElt){
	try{
		var citation = null;
		try{
			citation = Array.from(resqmlElt.attributes).filter(att => att.name.includes(".Citation"))[0];
		}catch(exceptCitation){}
		var title = "";
		try{
			title = Array.from(citation.properties).filter(att => att.name.startsWith(".Citation.Title"))[0].value;
		}catch(exceptTitle){}
		return title;
	}catch(except){console.log(except);console.log(resqmlElt);}
	return "";
}

export function getResqmlEltTitleText(resqmlElt){
	var tabulatioHeaderText = resqmlElt.rootUUID;
	try{
		var regexTypeReplace = /[a-z]+/g;

		var type = resqmlElt.type;
		if(type.includes("."))
			type = type.substring(type.lastIndexOf('.')+1);

		// on enlève les lettre non majuscules du type
		type = "[" + type.replace(regexTypeReplace, '') + "] ";

		var title = getResqmlEltCitationTitle(resqmlElt);
		tabulatioHeaderText = type + title + "(" + resqmlElt.rootUUID + ")";

	}catch(except){console.log(except);console.log(resqmlElt);}
	return tabulatioHeaderText;
}

export function getResqmlEltTabText(resqmlElt){
	var tabulatioHeaderText = getResqmlEltTitleText(resqmlElt);

	const MAX_SIZE = 20;
	if(tabulatioHeaderText.length > MAX_SIZE)
		tabulatioHeaderText = tabulatioHeaderText.substr(0, MAX_SIZE-3)+"...";
	return tabulatioHeaderText;
}

export function updateListEltIdx(elt_html, resqmlElt){
	if(resqmlElt!=null){
		var chIdx = getHtmlEltIdxInParent(elt_html);
		var previousIdx = parseInt(resqmlElt.name.substring(resqmlElt.name.lastIndexOf(".")+1));
		// On evite de changer si l'index n'a pas été modifié
		if(previousIdx!= chIdx){
			//console.log('idx : ' + chIdx + " named " + resqmlElt.name);
			var getUrl = "ObjectEdit?command=movelistelement"
								+ "&Root_UUID=" + resqmlElt.rootUUID
								+ "&path=" + resqmlElt.name
								+ "&index=" + chIdx;
			//console.log("sending get at url : " + getUrl);
			//resqmlElt.parentElt.updateListAttributesNames();
			//sendGetURL_Promise(getUrl).then(resqmlElt.parentElt.attributes.map(x => x.refresh()) );
			sendGetURL_Promise(getUrl).then(resqmlElt.parentElt.refresh());
		}
	}
}

export class ResqmlElement{

	

	/*constructor(){
		this.htmlAttributeElt = null;	// balise html de l'element courant dans l'arbre de visualisation
		this.htmlTitleElt = null; 		// balise html du titre de l'objet
		this.htmlSubAttribCreatorList = null;  // balise html qui liste les sous élement qu'il est possible de créer
		this.htmlSubAttribElt = null; 	// balise html contenant tous les sous-attributs
		this.htmlPropertyElt = null;	// balise html des proprietes de l'element courant

		this.name = "";			// chemin de l'element courant depuis l'element root (e.g. 'Citation.Title', 'TrianglePatch.0.Count' etc.)
		this.type = "";			// type de l'element courant
		this.value = null;		// valeur (String) de l'element courant
		this.typeValues = null;	// liste des valeurs possibles prises par le type de l'element courant (pour les enumerations)
		this.attributes = [];	// liste des sous-elements qui ont eux même des sous-elements
		this.properties = [];	// liste des elements proprietes (qui n'ont pas de sous-elements)
		this.parentElt = null; 	// element parent 
		
		this.visible = true;	// boolean de visibilite de l'element html
		this.rootUUID = null;	// UUID de l'element Resqml racine 
		this.htmlElt_propertyRoot = null; // Element html devant contenir toutes les propriétés des objets de l'arbre
	}*/ 
	
	constructor(resqmlJSONContent, resqmlRootUUID, htmlElt_propertyRoot, parentElt, isAttribute){
		this.htmlAttributeElt = null;
		this.htmlTitleElt = null; 
		this.htmlSubAttribCreatorList = null; 
		this.htmlSubAttribElt = null; 
		this.htmlPropertyElt = null;
		this.htmlElt_propertyRoot = htmlElt_propertyRoot;
		this.htmlCurrentProperty = null;
		this.subAttribCollapserElt = null;		// Fleche qui permet de toggle l'affichage des sous-attributs
		this.parentElt = parentElt;
		this.rootUUID = resqmlRootUUID;
		if(isAttribute!=null){
			this.isAttribute = isAttribute;
		}else{
			this.isAttribute = true;
		}
		
		this.updateElementFromJSON(resqmlJSONContent);
		this.visible = true;
	}

	updateElementFromJSON(resqmlJSONContent){
		this.name = resqmlJSONContent.name;

		this.mandatory = resqmlJSONContent.mandatory;
		//console.log("mladat : " + resqmlJSONContent.mandatory);

		//console.log(resqmlJSONContent);

		if(this.name==null || this.name.length<=0 || !isNaN(this.name)){
			this.name = "";
		}
		this.type = resqmlJSONContent.type;
		if(this.type==null || this.type.length<=0 || !isNaN(this.type)){
			this.type = "";
		}
		this.value = resqmlJSONContent.value;

		this.templatedClass = resqmlJSONContent.templatedClass;
		
		if(this.attributes == null){ 		// Si les attributs n'ont jamais existés
			if(resqmlJSONContent.attributes != null){
				this.attributes = [];
				for(var attrib_Idx=0; attrib_Idx<resqmlJSONContent.attributes.length; attrib_Idx++){
					this.attributes.push(new ResqmlElement(resqmlJSONContent.attributes[attrib_Idx], 
															this.rootUUID, 
															this.htmlElt_propertyRoot,
															this, 
															true))
				}
			}
		}else if(this.isResqmlListType()){	// Si c'est une liste, il faut remettre à jour tous les fils
											// car leur numérotation à peut être changée
											// cela empeche de garder les fils de liste ouverts...
			//console.log("updating list");
			var visibleIdx = []
			if(this.attributes != null){
				for(var curAtt_Idx=0; curAtt_Idx < this.attributes.length; curAtt_Idx++){
					try{
					    if(this.attributes[curAtt_Idx].visible){
					        visibleIdx.push(curAtt_Idx)
					    }
						this.attributes[curAtt_Idx].remove(false);
					}catch(exceptRemove){
						console.log(exceptRemove);
					}
				}
			}
			
			this.attributes = [];

			if(resqmlJSONContent.attributes != null){
				for(var attrib_Idx=0; attrib_Idx < resqmlJSONContent.attributes.length; attrib_Idx++){
					this.attributes.push(new ResqmlElement(resqmlJSONContent.attributes[attrib_Idx], 
															this.rootUUID, 
															this.htmlElt_propertyRoot,
															this, 
															true));
				}
			}
			for(var curAtt_Idx=0; curAtt_Idx < visibleIdx.length; curAtt_Idx++){
			    var cur_idx = visibleIdx[curAtt_Idx];
			    try{
			        this.attributes[cur_idx].visible = true;
			        this.attributes[cur_idx].subAttribCollapserElt.click();
			    }catch(exceptionVisible){
			        console.log(exceptionVisible);
			    }
			}
		}else{ 								// Si on met à jour un arbre existant
			// Si il y a des attributs

			var tabAttIdxToRemove = [];	// Ceux qui sont null doivent être enlevés de la vue mais pas de la liste
			var tabNullAttIdx = [];		// Ceux qui doivent être aussi enlevés de la liste des attributs

			if(resqmlJSONContent.attributes != null){
				for(var attrib_Idx=0; attrib_Idx < resqmlJSONContent.attributes.length; attrib_Idx++){
					var found = false;
					var attrib = resqmlJSONContent.attributes[attrib_Idx];
					for(var curAtt_Idx=0; curAtt_Idx < this.attributes.length; curAtt_Idx++){
						if(attrib.name.localeCompare(this.attributes[curAtt_Idx].name) == 0){
							// Si les deux attributs matchent, on update le sous-attribut
							found = true;
							this.attributes[curAtt_Idx].updateElementFromJSON(attrib);
						}
					}
					if(!found){ // Si on ne l'a pas trouvé on l'ajoute
						this.attributes.push(new ResqmlElement(resqmlJSONContent.attributes[attrib_Idx], 
																this.rootUUID, 
																this.htmlElt_propertyRoot,
																this, 
																true));
					}
				}
				// Test de ceux qu'il faut enlever
				for(var curAtt_Idx=0; curAtt_Idx < this.attributes.length; curAtt_Idx++){
					var found = false;
					for(var attrib_Idx=0; attrib_Idx < resqmlJSONContent.attributes.length; attrib_Idx++){
						if(resqmlJSONContent.attributes[attrib_Idx].name.localeCompare(this.attributes[curAtt_Idx].name) == 0){
							found = true;
							if( ResqmlElement_isNullAttribute(resqmlJSONContent.attributes[attrib_Idx])){
								// Si toujours la mais que valeur est nulle
								tabNullAttIdx.push(curAtt_Idx);
							}
							break;
						}
					}
					if(!found){
						// Si ce n'est pas un attribut null
						tabAttIdxToRemove.push(curAtt_Idx);
					}
				}
				
			}else{
				// si aucun attribut, on supprime ceux qui etaient la avant
				for(var curAtt_Idx=0; curAtt_Idx < this.attributes.length; curAtt_Idx++){
					tabAttIdxToRemove.push(curAtt_Idx);
				}
			}

			// On supprime de la vue ceux qui n'étaient pas null et donc présent dans l'arbre mais qui sont null a présent
			for(var attRemoveIdx=tabNullAttIdx.length-1; attRemoveIdx >=0; attRemoveIdx--){
				var removedElt = this.attributes[tabNullAttIdx[attRemoveIdx]];	 // Le retour est un tableau
				try{
					/*console.log("REM 1)");
					console.log(removedElt);*/
					removedElt.remove(false);
				}catch(exceptionNullAtt){
					console.log("ERRROR removing for : " + removedElt.name);
					console.log(exceptionNullAtt);
				}
			}
			// On parcours à partir de la fin pour commencer par les indices les plus elevés sinon on aurait un decalage au fur et a mesure
			for(var attRemoveIdx=tabAttIdxToRemove.length-1; attRemoveIdx >=0; attRemoveIdx--){
				var removedElt = this.attributes.splice(tabAttIdxToRemove[attRemoveIdx], 1);	 // Le retour est un tableau
				removedElt[0].remove(false); 
			}
			
		}

		if(this.properties == null){
			if(resqmlJSONContent.properties != null){
				this.properties = [];
				for(var prop_Idx=0; prop_Idx<resqmlJSONContent.properties.length; prop_Idx++){
					this.properties.push(new ResqmlElement(resqmlJSONContent.properties[prop_Idx], 
															this.rootUUID, 
															this.htmlElt_propertyRoot,
															this, 
															false))
				}
			}
		}else{ 								// Si on met à jour un arbre existant

			var tabPropIdxToRemove = [];

			// Si il y a des proprietes
			if(resqmlJSONContent.properties != null){
				for(var prop_Idx=0; prop_Idx < resqmlJSONContent.properties.length; prop_Idx++){
					var found = false;
					var prop = resqmlJSONContent.properties[prop_Idx];
					for(var curProp_Idx=0; curProp_Idx < this.properties.length; curProp_Idx++){
						if(prop.name.localeCompare(this.properties[curProp_Idx].name) == 0){
							//console.log("compare FOUND P : " + prop.name + " " + this.properties[curProp_Idx].name);
							// Si les deux proprietes matchent, on update
							found = true;
							this.properties[curProp_Idx].updateElementFromJSON(prop);
							break;
						}
					}
					if(!found){ // Si on ne l'a pas trouvé on l'ajoute
						this.properties.push(new ResqmlElement(resqmlJSONContent.properties[prop_Idx], 
																this.rootUUID, 
																this.htmlElt_propertyRoot,
																this, 
																false))
					}
				}
				// Test de ceux qu'il faut enlever
				for(var curProp_Idx=0; curProp_Idx < this.properties.length; curProp_Idx++){
					var found = false;
					for(var prop_Idx=0; prop_Idx < resqmlJSONContent.properties.length; prop_Idx++){
						if(resqmlJSONContent.properties[prop_Idx].name.localeCompare(this.properties[curProp_Idx].name) == 0){
							found = true;
							break;
						}else{
							/*console.log("compare FOUND P : " + resqmlJSONContent.properties[prop_Idx].name 
								+ " " + this.properties[curProp_Idx].name);*/
						}
					}
					if(!found){
						tabPropIdxToRemove.push(curProp_Idx);
					}
				}
				
			}else{
				// si aucune propriete, on supprime ceux qui etaient la avant
				for(var curProp_Idx=0; curProp_Idx < this.properties.length; curProp_Idx++){
					tabPropIdxToRemove.push(curProp_Idx);
				}
			}
 
			// On parcours à partir de la fin pour commencer par les indices les plus elevés sinon on aurait un decalage au fur et a mesure
			for(var propRemoveIdx=tabPropIdxToRemove.length - 1; propRemoveIdx >= 0 ; propRemoveIdx--){
				var removedElt = this.properties.splice(tabPropIdxToRemove[propRemoveIdx], 1);
				removedElt[0].remove(false);
			}
		}

		const eltValueNameAsProp = "_";
		if(this.properties!=null){
			for(var propRemoveIdx=0; propRemoveIdx < this.properties.length; propRemoveIdx++){
				if(this.properties[propRemoveIdx].name.endsWith(eltValueNameAsProp)){
					var removedElt = this.properties.splice(propRemoveIdx, 1);
					removedElt[0].remove(false);
				}
			}
		}

		if(		this.isAttribute 
			&& !this.isResqmlListType() 
			&&  this.value != null 
			&& (this.properties == null || this.properties.length==0)
			){
			this.properties = [new ResqmlElement(resqmlJSONContent, 
																this.rootUUID, 
																this.htmlElt_propertyRoot,
																this, 
																false)];
		}


		if(this.isResqmlListType() && (this.attributes == null || this.attributes.length <= 0) ){
			this.remove(false);
		}else{
			this.createView();
		}
	}


	createView(){
		const constThis = this;
		var isUpdating = false;


		// Nom a afficher : la derniere partie du path
		var shortName = this.name;
		if(shortName.includes("."))
			shortName = shortName.substring(shortName.lastIndexOf('.')+1);

		var shortType = this.type;
		if(shortType.includes("."))
			shortType = shortType.substring(shortType.lastIndexOf('.')+1);

		/*
			   _____ _        _            __                                                     _      __
			  / ___/(_)  ____( )___  _____/ /_   __  ______  ___     ____  _________  ____  _____(_)__  / /____
			  \__ \/ /  / ___/// _ \/ ___/ __/  / / / / __ \/ _ \   / __ \/ ___/ __ \/ __ \/ ___/ / _ \/ __/ _ \
			 ___/ / /  / /__  /  __(__  ) /_   / /_/ / / / /  __/  / /_/ / /  / /_/ / /_/ / /  / /  __/ /_/  __/
			/____/_/   \___/  \___/____/\__/   \__,_/_/ /_/\___/  / .___/_/   \____/ .___/_/  /_/\___/\__/\___/
			                                                     /_/              /_/
		*/
		if(!this.isAttribute){

			// Creation d'une case de tableau
			if(this.htmlAttributeElt == null){	// On crée l'element si il n'existe pas deja
				this.htmlAttributeElt = document.createElement("tr");
			}else{
				// Si existe deja on fait un e mise à jour
				isUpdating = true;
				// On enlève le contenu de l'ancien titre
				while (this.htmlAttributeElt.firstChild) {
					this.htmlAttributeElt.removeChild(this.htmlAttributeElt.firstChild);
				}
			}
			this.htmlAttributeElt.name = ResqmlElement_NAMESUBATTPREFIX + "_" + shortName;

			const col0 = document.createElement("td");
			const divCollapseLabel = document.createElement("label");
			if(this.mandatory=="true"){
				divCollapseLabel.className += "mandatoryElt";
			}
			divCollapseLabel.appendChild(document.createTextNode(shortName));
			

			var isExtEnum = false;
			var enumValues = mapResqmlEnumToValues[this.type.toLowerCase()]

			if(enumValues==null && this.parentElt != null){
				isExtEnum = true;
				var parentElt = this.parentElt;

				var parentResqmlType = this.parentElt.type.toLowerCase();

				while("java.lang.string" == parentResqmlType || parentElt.isResqmlListType()){
					parentElt = parentElt.parentElt;
					parentResqmlType = parentElt.type.toLowerCase();
				}

				var thisName = this.name.replace(/\.\d+/g, '').toLowerCase(); // On supprime les idx des listes (e.g. Tr.0.Throw.3 ==> Tr.Throw)
				if(thisName.includes(".")){
					thisName = thisName.substring(thisName.lastIndexOf(".")+1);
				}


				if(extTypeAttributes[parentResqmlType + "." + thisName]!=null){

					var realType = extTypeAttributes[parentResqmlType + "." + thisName];
					enumValues = mapResqmlEnumToValues[realType.toLowerCase()];
					if(enumValues == undefined && realType.toLowerCase().endsWith("ext")){
						// on essaie sans le "ext" a la fin
						enumValues = mapResqmlEnumToValues[realType.toLowerCase().substring(0, realType.length - 3)];
					}
					if(realType.includes(".")){
						realType = realType.substring(realType.lastIndexOf (".")+1);
					}

					divCollapseLabel.title = extTypeAttributes[realType];
				}else if(parentResqmlType.endsWith("ext") && extTypeAttributes[parentResqmlType.substring(0, parentResqmlType.length-3) + "." + thisName]!=null){
					var realType = extTypeAttributes[parentResqmlType.substring(0, parentResqmlType.length-3) + "." + thisName];
					// Si le ext n'existe pas, on cherche le type de base qui sera etendu
					enumValues = mapResqmlEnumToValues[realType.toLowerCase()];
					if(realType.includes(".")){
						realType = realType.substring(realType.lastIndexOf (".")+1);
					}
					divCollapseLabel.title = realType;
				}else if(!parentResqmlType.endsWith("ext") && extTypeAttributes[parentResqmlType + "ext." + thisName]!=null){
					var realType = extTypeAttributes[parentResqmlType + "ext." + thisName];
					// Si le ext n'existe pas, on cherche le type avec "ext" a la fin
					enumValues = mapResqmlEnumToValues[realType.toLowerCase()];
					if(realType.includes(".")){
						realType = realType.substring(realType.lastIndexOf (".")+1);
					}
					divCollapseLabel.title = realType;
				}
			}

			divCollapseLabel.title = shortType;
			col0.appendChild(divCollapseLabel);

			const col1 = document.createElement("td");

			if(enumValues!=null){ // si c'est un type enum 
				const divCollapseSelect = geosiris_createEditableSelector(enumValues, this.value, isExtEnum);
				divCollapseSelect.className = "form-control";
				divCollapseSelect.id = this.name; // Le chemin
				divCollapseSelect.name = this.name; // Le chemin

				col1.appendChild(divCollapseSelect);

			}else{ // ce n'est pas un type enum
				//console.log(this.name + " --> " + this.type + " :: " + (this.type.toLowerCase().includes('calendar')))

//				console.log(this.name + " --> " + this.type + " :: " + (this.type.toLowerCase().includes('calendar')))
				var divCollapseInput_TMP = null;
                if(this.parentElt.properties!=null && this.type.toLowerCase().includes("string") && (this.parentElt.properties.length==1 || (this.value != null && this.value.length > 100)) ){
                    // On met un champs text plus gros si c'est une chaine de caratere et que c'est la seule propriete.
                    divCollapseInput_TMP = document.createElement("textarea");
                    divCollapseInput_TMP.rows = 10;
                    //divCollapseInput.min
                }else{
                    divCollapseInput_TMP = document.createElement("input");
                }
                const divCollapseInput = divCollapseInput_TMP;
//				console.log("\tdivCollapseInput " + divCollapseInput)

				divCollapseInput.className = "form-control"; 	// Le chemin
				divCollapseInput.name = this.name; 	// Le chemin
				divCollapseInput.typeName = "text";
				if(this.value!=null){
					divCollapseInput.value = this.value;
				}
				//console.log("\tset value to " + this.value + " -- " + divCollapseInput.value)
				
				col1.appendChild(divCollapseInput);
				if(this.name.toLowerCase().endsWith('uuid') && this.value != this.rootUUID){
					divCollapseInput.style.display = 'inline-block';

					{// Bouton d'oeil pour ouvrir l'objet represente par l'uuid 
						const but_open_from_uuid = document.createElement("span");
						but_open_from_uuid.className = "openElementBut fas fa-eye";
						but_open_from_uuid.title = "Open";
						but_open_from_uuid.addEventListener("mouseover", function() {
							but_open_from_uuid.className = "openElementBut far fa-eye";
						});

						but_open_from_uuid.addEventListener("mouseout", function() {
							but_open_from_uuid.className = "openElementBut fas fa-eye";
						})
						but_open_from_uuid.onclick = function(){
							openResqmlObjectContentByUUID(constThis.value);
						}
						col1.appendChild(but_open_from_uuid);
						/*// On catch le clic droit
						divCollapseInput.oncontextmenu = function (event) {
							if(constThis.value!=null){
								console.log("test open")
								var popOverOpenUUID = getPopoverOpenUUID(constThis.value);
								popOverOpenUUID.className = "geosirisPopOver";
								document.body.append(popOverOpenUUID);
							}
							console.log("NO open")
							return false; //on annule l'affichage du menu contextuel
						}*/
					}
					{ // uuid generator<i class="fa-solid fa-wand-magic-sparkles"></i>
						const but_generate_uuid = document.createElement("span");
						but_generate_uuid.className = "genElementBut far fa-edit";
						but_generate_uuid.title = "Generate uuid (field MUST be empty!)";

						but_generate_uuid.addEventListener("mouseover", function() {
							but_generate_uuid.className = "genElementBut fas fa-edit";
						});

						but_generate_uuid.addEventListener("mouseout", function() {
							but_generate_uuid.className = "genElementBut fas fa-edit";
						})
						but_generate_uuid.onclick = function(){
							if(isUUID(divCollapseInput.value)){
								// On ne remplace pas si deja qqch d'ecrit
								createSnackBar("UUID field must be empty before generation", 1000);
							}else{
								constThis.value = generateUUID();
								divCollapseInput.value = constThis.value;
							}
						}
						col1.appendChild(but_generate_uuid);
					}
					divCollapseInput.pattern = UUID_REGEX_str;
				}else if (this.type.toLowerCase().includes('calendar')){
					divCollapseInput.style.display = 'inline-block';
					var but = document.createElement("span");
					but.className = "openElementBut fas fa-clock";
					but.title = "Now";
					but.addEventListener("mouseover", function() {
						but.className = "openElementBut far fa-clock";
					});

					but.addEventListener("mouseout", function() {
						but.className = "openElementBut fas fa-clock";
					})
					but.onclick = function(){
						// On veut une date en format : 2020-07-29T10:08:38.952+02:00
						const dateNow = new Date(Date.now());
						/*var timeNow = dateNow.toTimeString().replaceAll(' ', '');
						//timeNow = timeNow.substring(0, timeNow.indexOf('('));
						var month = dateNow.getUTCMonth()+1
						if((""+month).length<=1)
							month = "0"+month;
						var day = dateNow.getUTCDate();
						if((""+day).length<=1)
							day = "0"+day;
						var milisec = dateNow.getUTCMilliseconds();
						var strDate = dateNow.getUTCFullYear()+"-"+month+"-"+day+"T"+timeNow;*/
						var strDate = dateNow.toISOString();
						constThis.value= strDate;
						divCollapseInput.value = strDate;
					}
					col1.appendChild(but);
				}
			}
			this.htmlAttributeElt.appendChild(col0);
			this.htmlAttributeElt.appendChild(col1);
		}else{
		/*
			   _____ _        _            __                         ____           _ ____            __        ___             __                  __      __  __       _ __          __  _
			  / ___/(_)  ____( )___  _____/ /_   __  ______  ___     / __/__  __  __(_) / /__     ____/ /__     / ( )____ ______/ /_  ________     _/_/___ _/ /_/ /______(_) /_  __  __/ /_| |
			  \__ \/ /  / ___/// _ \/ ___/ __/  / / / / __ \/ _ \   / /_/ _ \/ / / / / / / _ \   / __  / _ \   / /|// __ `/ ___/ __ \/ ___/ _ \   / // __ `/ __/ __/ ___/ / __ \/ / / / __// /
			 ___/ / /  / /__  /  __(__  ) /_   / /_/ / / / /  __/  / __/  __/ /_/ / / / /  __/  / /_/ /  __/  / /  / /_/ / /  / /_/ / /  /  __/  / // /_/ / /_/ /_/ /  / / /_/ / /_/ / /_ / /
			/____/_/   \___/  \___/____/\__/   \__,_/_/ /_/\___/  /_/  \___/\__,_/_/_/_/\___/   \__,_/\___/  /_/   \__,_/_/  /_.___/_/   \___/  / / \__,_/\__/\__/_/  /_/_.___/\__,_/\__//_/
			                                                                                                                                    |_|                                    /_/
		*/
			// Creation d'une div pour l'element courant et ses attributs
			if(this.htmlAttributeElt == null){	// On crée l'element si il n'existe pas deja
				this.htmlAttributeElt = document.createElement("div");
			}else{
				// Si existe deja on fait une mise à jour
				isUpdating = true;
				if(this.isResqmlListType() && (this.attributes == null || this.attributes.length <= 0) ){
					// Si c'est une liste et qu'elle est vide, on supprime l'element
					// this.remove();
					return null;
				}
			}
			this.htmlAttributeElt.name = ResqmlElement_NAMESUBATTPREFIX + "_" + shortName;

			if(this.type.includes("CustomData")){
				// On enlève le contenu de l'ancien titre
				if(isUpdating){
					while (this.htmlAttributeElt.firstChild) {
						this.htmlAttributeElt.removeChild(this.htmlAttributeElt.firstChild);
					}
				}
				var italicDiv = document.createElement("i");
				italicDiv.className = "fa fa-exclamation-triangle";
				italicDiv.title = "Check object's xml translation to see the custom data";
				italicDiv.appendChild(document.createTextNode("CustomData not supported"));
				this.htmlAttributeElt.appendChild(italicDiv);
			}else{

				/** Attributs **/

				// Creation des sous-elements : 
				var tabCreateSubAttrib  = [];
				var tabListsAttribs = [];

				if(!isUpdating){
					this.htmlSubAttribElt = document.createElement("div");
					if(this.isResqmlListType()){
						this.htmlSubAttribElt.className = "dropper";
					}
				}

				if(this.attributes != null && this.attributes.length>0){
					for(var attIdx=0; attIdx < this.attributes.length; attIdx++){
						const subA = this.attributes[attIdx];
						if(ResqmlElement_isNullAttribute(subA)){	
							// Les elements que l'on pourra créer
							tabCreateSubAttrib.push(subA.getCurrentListContentElt());
							//console.log("# Null attribt : " + subA.type);
						}else{
							const subAElt = subA.createView();
							if(subAElt != null){
								if(subA.isResqmlListType()){
									//console.log("IS LIST : " );
									if(subA.attributes!=null && subA.attributes.length>0){
										this.htmlSubAttribElt.appendChild(subAElt);
									//console.log(" ----------- " );
									}
									//console.log(subA);
									tabListsAttribs.push(subA.getCurrentListContentElt());
								}else{
									this.htmlSubAttribElt.appendChild(subAElt);
									if(this.isResqmlListType()){
										const currentDraggableElt = subA; 
										const currentDraggableElt_html = subAElt;
										subAElt.className = "draggableElt";
										if(!subAElt.lastChild.previousSibling.className.includes("draggableButton") && this.attributes.length > 1){
											var butDrag = document.createElement("span");
											butDrag.className = "draggableButton";
											butDrag.name = this.name;
											const constButDrag = butDrag;
											geosiris_DndHandler.applyDragEvents(constButDrag, function(){updateListEltIdx(currentDraggableElt_html, currentDraggableElt);});
											subAElt.insertBefore(butDrag, subAElt.lastChild);
										}
									}
								}
							}else{
								tabCreateSubAttrib.push(subA.getCurrentListContentElt());
							}
						}
					}


					// Pour les draggables 
					if(this.isResqmlListType()){
						geosiris_DndHandler.applyDropEvents(this.htmlSubAttribElt);
					}
				}


				

				//console.log("List for : " + this.name + " is ");console.log(tabListsAttribs);
				//console.log("ResqmlElement_isNullAttribute: " + this.name + " is ");console.log(tabCreateSubAttrib);
				// Creation du titre
				if(!isUpdating){
					this.htmlTitleElt = document.createElement("span");
					if(this.mandatory=="true"){
						this.htmlTitleElt.className += " mandatoryElt";
					}
				}else{
					if(this.htmlTitleElt!=null){
						// On enlève le contenu de l'ancien titre
						while (this.htmlTitleElt.firstChild) {
							this.htmlTitleElt.removeChild(this.htmlTitleElt.firstChild);
						}
					}else{
						console.log("null title");
						console.log(this);
					}
				}

				var showTypeAsTooltip = false;

				var typeInTitle = "[" + shortType + "] "

				if(this.parentElt != null){
					if(shortType.endsWith("List")){
						typeInTitle = "";
					}else if(shortType.length + shortName.length > 25){
						typeInTitle = typeInTitle.replace( /[a-z]/g, '' )
						showTypeAsTooltip = true;
					}
				}else{ // the Top title
					var title = getResqmlEltCitationTitle(this);
					if (title != null && title.length > 0){
						typeInTitle += " '" + title + "'";
					}
					typeInTitle += " " + this.rootUUID;
				}


				var booleanInTitle = ""
				if(this.properties != null){
					// Search boolean properties to print in title
					for(var propIdx=0;propIdx<this.properties.length; propIdx++){
						var prop = this.properties[propIdx];
						if(prop.type.toLowerCase().endsWith("boolean")
							&& (prop.value.toLowerCase()=="true" 
								|| prop.value.toLowerCase()=="yes" 
								|| prop.value.toLowerCase()=="1") 
							){
							var propName = prop.name;
							propName = propName.substring(propName.lastIndexOf('.')+1);
							if(propName.startsWith("Is") && propName.length > 2){
								propName = propName.substring(2);
							}
							booleanInTitle += " {" + propName + "} ";
						}
					}
				}

				var titleValue =  booleanInTitle + shortName ;
				/*console.log('This title : ' + titleValue + " -- ")
				console.log(this.properties)*/

				if(!isNaN(parseInt(shortName)) || this.type.toLowerCase().includes("dataobjectreference")){
					// On cherche si pour les elements des listes on peut avoir un meilleur nom
					// Pour tous les fils, on regarde si il existe un element Title
					if(this.properties != null){
						var found_title = false;
						for(var propIdx=0; propIdx < this.properties.length; propIdx++){
							const subProp = this.properties[propIdx];
							var subPropName = subProp.name;
							if(subPropName.includes(".")){
								subPropName = subPropName.substring(subPropName.lastIndexOf('.')+1);
							}
							if(subPropName.toLowerCase() == "title"){
								titleValue += " '" + subProp.value + "'";
								found_title = false;
								break;
							}
						}
						// Si on ne trouve pas de title, on cherche une value (e.g. les value dans les StringTableLookup)
						if(!found_title){
							for(var propIdx=0; propIdx < this.properties.length; propIdx++){
								const subProp = this.properties[propIdx];
								var subPropName = subProp.name;
								if(subPropName.includes(".")){
									subPropName = subPropName.substring(subPropName.lastIndexOf('.')+1);
								}
								if(subPropName.toLowerCase() == "value"){
									titleValue += " <" + subProp.value + ">";
									found_title = false;
									break;
								}
							}
						}
					}
					if(this.attributes!=null){
						for(var attIdx=0; attIdx < this.attributes.length; attIdx++){
							const subA = this.attributes[attIdx];
							if(subA.properties != null && subA.properties.length > 0){
								var subA_shortName = subA.name;
								if(subA_shortName.includes(".")){
									subA_shortName = subA_shortName.substring(subA_shortName.lastIndexOf('.')+1);
								}
								for(var propIdx=0; propIdx < subA.properties.length; propIdx++){
									const subProp = subA.properties[propIdx];
									var subPropName = subProp.name;
									if(subPropName.includes(".")){
										subPropName = subPropName.substring(subPropName.lastIndexOf('.')+1);
									}
									if(subPropName.toLowerCase() == "title"){
										titleValue += " " + subA_shortName.replace( /[a-z]/g, '' ) + "(" + subProp.value + ")";
										break;
									}
								}
							}
						}
					}
				}


				// Title type
				var htmlTitleElt_type = document.createElement("span");
				htmlTitleElt_type.appendChild(document.createTextNode(typeInTitle));
				this.htmlTitleElt.appendChild(htmlTitleElt_type);

				if(mapResqmlTypesComment[this.type.toLowerCase()] != null){
					var commentType = document.createElement("span");
					commentType.className = "typeCommentIcon fas fa-question-circle";
					this.htmlTitleElt.appendChild(commentType);
					var divCommentTitle = createHoverableHtmlContent(commentType, mapResqmlTypesComment[this.type.toLowerCase()]);
					
					this.htmlTitleElt.appendChild(divCommentTitle);
				}else{
					//console.log("no comment for " + this.type);
				}

				var htmlTitleElt_values = document.createElement("span");
				htmlTitleElt_values.appendChild(document.createTextNode(titleValue));
				this.htmlTitleElt.appendChild(htmlTitleElt_values);
				if(showTypeAsTooltip){
					this.htmlTitleElt.title = shortType;
				}
				

				if(!isUpdating){
					// Creation du bouton de collapser d'arbre
					this.subAttribCollapserElt = document.createElement("span");
					//this.subAttribCollapserElt.id = this.rootUUID + this.name;
					this.subAttribCollapserElt.style.cursor = "pointer";
					this.subAttribCollapserElt.className    = "resqmlCollapse fas fa-chevron-right";

					this.subAttribCollapserElt.onclick = 
						function(){
							if(constThis.htmlSubAttribElt != null){
								if(constThis.subAttribCollapserElt.className.includes("fa-chevron-right")){
									constThis.subAttribCollapserElt.className = constThis.subAttribCollapserElt.className.replace("fa-chevron-right", "fa-chevron-down");
									constThis.htmlSubAttribElt.style.display  = "";
								}else{
									constThis.subAttribCollapserElt.className = constThis.subAttribCollapserElt.className.replace("fa-chevron-down", "fa-chevron-right");
									constThis.htmlSubAttribElt.style.display  = "none";
								}
							}
						};

					this.htmlAttributeElt.appendChild(this.subAttribCollapserElt);

					this.htmlAttributeElt.appendChild(this.htmlTitleElt);

					if(constThis.type.toLowerCase().includes("datasetpart")
						|| constThis.type.toLowerCase().includes("hdf5dataset")){
						var butOpenHDFView = document.createElement("img");
						butOpenHDFView.src = "ressources/img/HDF_logo.png";
						butOpenHDFView.alt = "[ HDFView ]";
						butOpenHDFView.className = "hdfViewIcon"
						butOpenHDFView.title = "Open in HDFView"
						//var butOpenHDFView = document.createElement("span");
						//butOpenHDFView.appendChild(document.createTextNode("[[HDFView]]"));
						//butOpenHDFView.appendChild(document.createTextNode(""));
						butOpenHDFView.onclick = 	function(){
														//console.log(constThis);
														var pathInHDF = "";
														for(var propIdx=0; propIdx<constThis.properties.length; propIdx++){
															if(constThis.properties[propIdx].name.toLowerCase().endsWith("pathinhdffile")
																|| constThis.properties[propIdx].name.toLowerCase().endsWith("pathinexternalfile"))
															{
																//console.log(constThis.properties[propIdx]);
																pathInHDF = constThis.properties[propIdx].value;
																break;
															}
														}

														for(var attIdx=0; attIdx<constThis.attributes.length; attIdx++){
															var cur_attrib = constThis.attributes[attIdx]; 
															if(cur_attrib.name.toLowerCase().endsWith("epcexternalpartreference")){
																var extUuid = ""
																for(var propIdx=0; propIdx<cur_attrib.properties.length; propIdx++){
																	if(cur_attrib.properties[propIdx].name.toLowerCase().endsWith(".uuid"))
																	{
																		//console.log("uuid found")
																		extUuid = cur_attrib.properties[propIdx].value;
																		break;
																	}
																}
																try{
																	cur_attrib.getResqmlObjectJson(extUuid, "Filename").then(
																		res => {
																			openHDFViewAskH5Location(res.value, pathInHDF);
																		});
																}catch(e){
																	console.log(e);
																}
															}
														}
														/*openHDFViewAskH5Location(pathInHDF);
														console.log("===> HDF5 open")
														console.log(constThis.attributes)*/
												 	};
						this.htmlAttributeElt.appendChild(butOpenHDFView);
					}


					// Si il y a des sous-element a creer
					if(tabCreateSubAttrib.length>0 || tabListsAttribs.length>0){
						this.htmlSubAttribCreatorList = this.createSubAttribAddingListCollapser(tabCreateSubAttrib.concat(tabListsAttribs));
						if(this.htmlSubAttribCreatorList != null){
							this.htmlAttributeElt.appendChild(this.htmlSubAttribCreatorList);
						}
					}

					if(!this.isResqmlListType() && this.name.length>0){
						// Bouton de suppression
						var deleteBut = createDeleteButton("deleteButton deleteButtonTree", "Delete Element");
						deleteBut.onclick = function(){constThis.remove(true);};
						this.htmlAttributeElt.appendChild(deleteBut);
					}

					if(this.htmlSubAttribElt != null){
						this.htmlAttributeElt.appendChild(this.htmlSubAttribElt);
						this.htmlSubAttribElt.style.display = "none";
						this.htmlSubAttribElt.className += " treeSubAttribute";
					}

				}else{	// updating
					// Si il y a des sous-element a creer
					if(tabCreateSubAttrib.length>0 || tabListsAttribs.length>0){
						var newCreatorList = this.createSubAttribAddingListCollapser(tabCreateSubAttrib.concat(tabListsAttribs), this);
						if(this.htmlSubAttribCreatorList != null){	// Si il y en avait un avant on le remplace
							if(newCreatorList != null){
								this.htmlAttributeElt.replaceChild(newCreatorList, this.htmlSubAttribCreatorList);
								try{
									this.htmlSubAttribCreatorList.remove();
								}catch(Ee){console.log(Ee);}
							}else{
								this.htmlAttributeElt.remove();
							}
						}else{	// Si il y en avait pas avant on l'ajoute apres le title 
							if(newCreatorList != null){
								this.htmlTitleElt.after(newCreatorList);
							}
						}
						this.htmlSubAttribCreatorList = newCreatorList;
					}else{
						if(this.htmlSubAttribCreatorList != null){
							this.htmlSubAttribCreatorList.remove();
							this.htmlSubAttribCreatorList = null;
						}
					}
				}

				// Si il y a des sous-attributs on affiche la fleche, sinon on ne l'affiche pas
				if(this.htmlSubAttribElt.children.length <= 0){
					this.subAttribCollapserElt.style.display = "none";
					this.subAttribCollapserElt.className = this.subAttribCollapserElt.className.replace("fa-chevron-down", "fa-chevron-right");
					this.htmlSubAttribElt.style.display = "none";
				}else{
					this.subAttribCollapserElt.style.display = "";
				}
				

				/** Property **/
				if((this.properties!= null && this.properties.length > 0) || this.value != null){
					const propRootElt = this.htmlElt_propertyRoot;
					if(propRootElt != null){
						var lastStyleDisplay = "none";
						if(this.htmlCurrentProperty!=null){
							lastStyleDisplay = this.htmlCurrentProperty.style.display;
							this.htmlCurrentProperty.remove();
						}

						this.htmlCurrentProperty = this.createPropertyTable();
						this.htmlCurrentProperty.style.display = lastStyleDisplay;
						propRootElt.appendChild(this.htmlCurrentProperty);

						const constCurrentProp = this.htmlCurrentProperty;
						
						this.htmlTitleElt.style.cursor = "pointer";
						if(!this.htmlTitleElt.className.includes("treeLeafWithProperty"))
							this.htmlTitleElt.className += "  treeLeafWithProperty";
						//this.htmlTitleElt.style.fontStyle = "italic";

						this.htmlTitleElt.onclick = function(){
							for(var propChildIdx=0; propChildIdx < propRootElt.childNodes.length; propChildIdx++){
								try{
									// on invisibilise toutes les propriétés
									propRootElt.childNodes[propChildIdx].style.display = "none";
									// console.log("child disable : " + propRootElt.childNodes[propChildIdx] + " " + propChildIdx);
								}catch(e){console.error("/!\\ child not disable : " + propChildIdx);}
							} 
							//console.log(constCurrentProp);
							constCurrentProp.style.display = "";
						};
					}else{
	//					 console.log("no property ROOT " + this.name);
					}
				}else{
					// console.log("no property for " + this.name);
					// Si on a pas de propriete on active le click pour ouvrir les sous element a la place.
					const constThis = this;
					this.htmlTitleElt.onclick = function(){
						constThis.subAttribCollapserElt.click();
					};
				}

				if(!isUpdating && this.parentElt == null){
					this.subAttribCollapserElt.click();
				}
			}
		}

		/** **/
		return this.htmlAttributeElt;
	}


	createPropertyTable(){
		var shortName = this.name;
		if(shortName.includes("."))
			shortName = shortName.substring(shortName.lastIndexOf('.')+1);
		if(shortName.length<=0){
			shortName = this.rootUUID;
		}

		// En-tete
		var titleElt = document.createElement("h4");
		titleElt.appendChild(document.createTextNode("Properties of " + shortName));
		titleElt.title = this.name;

		// Tableau
		var propTable = document.createElement("table");
		propTable.className+="table-striped table-bordered table-hover propertyTable";
		
		if(this.properties != null){
			for(var prop=0; prop<this.properties.length; prop++){
				const propValue = this.properties[prop];
				//console.log("Prop creation : " + propValue.name);

				propTable.appendChild(propValue.createView());
			}
		}else if(this.data != null && !this.isResqmlListType()){
			// Pour les element propriete qui apparaissent quand meme dans l'arbre : 
			// ex : ObjActivityTemplate.Parameter.0.KeyConstraint.0 : ce sont des String qui doivent etre dans l'arbre

			propTable.appendChild(this.createView());
			//console.log(' adding elt : ' + this);
		}

		var divId = "prop_" + this.rootUUID + this.name;
		const propertyDiv = document.createElement("div");
		propertyDiv.id = divId;
		propertyDiv.className = "propertyContainer";


		if(this.type.toLowerCase().includes("dataobjectreference")){
			try{
				propTable.appendChild(document.createElement("hr"));

				const line = document.createElement("tr");

				const col0_uri = document.createElement("td");
				const col0_uri_label = document.createElement("label");
				col0_uri_label.appendChild(document.createTextNode("URI (generated)"));
				col0_uri.appendChild(col0_uri_label);
				line.appendChild(col0_uri);

				var contentType = null;
				var qualifiedtype = null;
				var contentUUID = "###";
				if(this.properties != null){
					for(var prop=0; prop<this.properties.length; prop++){
						const propValue = this.properties[prop];
						if(propValue.name.toLowerCase().endsWith("contenttype")){
							contentType = propValue.value
						}else if(propValue.name.toLowerCase().endsWith("qualifiedtype")){
							qualifiedtype = propValue.value
						}
						if(propValue.name.toLowerCase().endsWith("uuid")){
							contentUUID = propValue.value
						}
					}
				}
				try{
					var obj_version_num = "";
					var obj_pkg = "";
					var obj_type = "";
					if(contentType != null){
						const regex_version = /version=([0-9]+(\.[0-9]+))/i;
						const regex_pkg = /\/x-([a-zA-Z]+)/i;
						const regex_type = /type=([a-zA-Z_]+)/i;
						obj_version_num = contentType.match(regex_version)[1];
						obj_pkg = contentType.match(regex_pkg)[1];
						obj_type = contentType.match(regex_type)[1];
					}else if(qualifiedtype != null){
						const regex_qualified = /(?<pkgName>\w+)(?<version>[\d][\d])\.(?<objecType>\w+)/mi;
						const match = qualifiedtype.match(regex_qualified);
						obj_version_num = match.groups["version"];
						obj_pkg = match.groups["pkgName"];
						obj_type = match.groups["objecType"];
					}
					const generated_uri = "eml:///" + obj_pkg + obj_version_num.replace(".", "") + "." + obj_type + "(" + contentUUID + ")";

					const col1_uri = document.createElement("td");
					const col1_uri_text = document.createElement("input");
					col1_uri_text.readOnly = "readonly";
					col1_uri_text.className = "form-control"; 	// Le chemin
					col1_uri_text.typeName = "text";
					col1_uri_text.value = generated_uri;
					col1_uri.appendChild(col1_uri_text);
					line.appendChild(col1_uri);
					propTable.appendChild(line);
				}catch(Except){
					console.log("unable to generate URI");
				}

			}catch(Except){
				console.log(Except);
			}
		}


		propertyDiv.appendChild(titleElt);
		propertyDiv.appendChild(propTable);

		return propertyDiv;
	}


	getCurrentListContentElt(){
		if(this.isResqmlListType()){
			var contentJSON = "{\"name\":\"" + this.name  + "\", "
							+ " \"type\":\"" + this.templatedClass[0] + "\"}";

			var res = new ResqmlElement(JSON.parse(contentJSON), 
										this.rootUUID, 
										this.htmlElt_propertyRoot,
										this, 
										true);
			return res;
		}else{
			//console.log("not a list type " + this.name +" - " + this.type);
		}
		return this;
	}

	refresh(){
		//console.log("refreshing " + this.name);
		const constThis = this;

		var url = "ResqmlObjectTree?uuid="+this.rootUUID;
		if(this.name.length > 0){
			url += "&path=" + this.name;
		}
		//console.log("UPDATING sending url " + url);
		return sendGetURL_Promise(url).then(
						responseText =>  new Promise((resolve, reject) =>{
							if(responseText != null){
								//console.log(responseText);
								try{
									var jsonContent = JSON.parse(responseText);
									constThis.updateElementFromJSON(jsonContent);
									resolve();
								}catch(ExceptionJSON){reject();}
							}else{
								reject();
							}
						}));
	}

	getResqmlObjectJson(uuid, subPath){
		//console.log("refreshing " + this.name);

		var url = "ResqmlObjectTree?uuid="+uuid;
		if(subPath && subPath.length > 0){
			url += "&path=" + subPath;
		}
		//console.log("getResqmlObjectJson sending url " + url);
		return sendGetURL_Promise(url).then(
						responseText =>  new Promise((resolve, reject) =>{
							if(responseText != null){
								//console.log("RESPONSE : " + responseText);
								try{
									var jsonContent = JSON.parse(responseText);
									resolve(jsonContent);
								}catch(ExceptionJSON){reject();}
							}else{
								reject();
							}
						}));
	}

	isLeafProperty(){
		return  (this.properties == null || this.properties.length <= 0)
			&&	(this.attributes == null || this.attributes.length <= 0)
			&&  !this.isResqmlListType();
	}

	isResqmlListType(){
		return this.type.endsWith("List");// || this.type.endsWith("Array"); // ou Collection
	}

	remove(updateServer){
		const constThis = this;
		/*console.log("> REMOVING ");
		console.log(this);*/

		if(updateServer!=null && updateServer){
			var url = "ObjectEdit?Root_UUID=" + this.rootUUID 
						+ "&command=delete"
						+ "&path=" + this.name;
			//console.log("remove and request server : " + this.name + " URL : " + url);
			fetch(url).then(function(){
				if(constThis.parentElt != null){
					/*console.log("fini delete");
					console.log("refresh parent : ");
					console.log(constThis.parentElt);*/
					constThis.parentElt.refresh();
				}else{
					console.log("null parent for refresh");
					console.log(constThis);
				}
			});
		}else{
			if(this.htmlAttributeElt != null && this.htmlAttributeElt.parentNode != null){
				//console.log("removing elt : " + this.name);
				//this.htmlAttributeElt.remove();
				this.htmlAttributeElt.parentNode.removeChild(this.htmlAttributeElt);
				this.htmlAttributeElt = null;
				//this.htmlElt_propertyRoot.remove();
				//this.htmlElt_propertyRoot = null;
			}else{
				/*console.log("null html for ");
				console.log(this);*/
			}
			if(this.htmlCurrentProperty!=null){
				//console.log("removing all element with id starting with : " + this.htmlCurrentProperty);
				document.querySelectorAll('[id^="' + this.htmlCurrentProperty.id + '"]').forEach( 
					function(element, index) {
						element.remove();
					});
			}
		}
	}

	// Ici on se sert d'une liste globale qui contient tous les types existants
	createSubAttribAddingListCollapser(subAttList, currentElt){
		var tabSubAttElt = [];

		// On tris les element à creer
		subAttList.sort(function (a, b) {
		  return a.name.localeCompare(b.name);
		});


		for(var subA_Idx=0; subA_Idx<subAttList.length; subA_Idx++){
			var subA = subAttList[subA_Idx];
			if(mapResqmlTypeToSubtypes[subA.type.toLowerCase()]!=null){
				for(var nbInstanciableType=0; nbInstanciableType<mapResqmlTypeToSubtypes[subA.type.toLowerCase()].length; nbInstanciableType++){
					var subEltType = mapResqmlTypeToSubtypes[subA.type.toLowerCase()][nbInstanciableType];

/*
					    ___  ___________________   ________________  _   __
					   /   |/_  __/_  __/ ____/ | / /_  __/  _/ __ \/ | / /
					  / /| | / /   / / / __/ /  |/ / / /  / // / / /  |/ /
					 / ___ |/ /   / / / /___/ /|  / / / _/ // /_/ / /|  /
					/_/  |_/_/   /_/ /_____/_/ |_/ /_/ /___/\____/_/ |_/
*/

					// On filtre les ContactElement[Reference] car ils etendent DOR et sont proposés partout alors qu'il ne sont 
					// utiles QUE dans les BinaryContactInterpretationPart (dans les StructuralOrgInterp et StratigraphiqOrgInterp)

					// Ici on a choisi de bypasser le CER si le type de l'element courant ne contient pas le mot "Contact"
					if(!subEltType.toLowerCase().includes("contactelement") 
						|| this.type.toLowerCase().includes("contact")){
						tabSubAttElt.push(this.createSubAttribAddingElt(subEltType, subA.name, currentElt));
						if(subA.mandatory=="true"){
							tabSubAttElt[tabSubAttElt.length-1].className += " mandatoryElt";
						}
					}

					/*console.log(">>>>" + mapResqmlTypeToSubtypes[subA.type.toLowerCase()][nbInstanciableType] + " + " + subA.name + " + " + subA.type);
					console.log(mapResqmlTypeToSubtypes);*/
				}
			}else if("java.lang.Object" != subA.type){
				if(!subA.type.toLowerCase().includes("contactelement") || this.type.toLowerCase().includes("contact")){
					tabSubAttElt.push(this.createSubAttribAddingElt(subA.type, subA.name, currentElt));
					if(subA.mandatory=="true"){
						tabSubAttElt[tabSubAttElt.length-1].className += " mandatoryElt";
					}
				}else{
					console.log('On a oublié contactelement[reference] pour le type ' + this.type);
				}
				/*console.log(mapResqmlTypeToSubtypes)
				console.log(">-->" +subA.type + " + " + subA.name);*/
			}else{
				console.log("Error for subElement '" + subA.name + "'' creation. Editor doesn't support " + subA.type + " creation");
			}
		}
		if(tabSubAttElt.length>0){
			var dropDown = createDropDownButton(tabSubAttElt, "subAttributeCreatorList_"+this.name, "dropdown-menu-energyml-elt");
			dropDown.title = "Create sub element";
			dropDown.className += " dropDownCreateSubAttribute";
			return dropDown;
		}else{
			return null;
		}
	}

	createSubAttribAddingElt(subEltType, subEltName, currentElt){
		const constThis = this;
		const constSubEltName = subEltName;

		var link = document.createElement("span");
		//link.id = id;
		var typeName = subEltType;
		if(typeName.includes(".")){
			typeName = typeName.substr(typeName.lastIndexOf(".")+1);
		}
		var parentName = this.name;
		if(parentName.includes(".")){
			parentName = parentName.substr(0, parentName.lastIndexOf("."));
		}

		var currentName = subEltName;
		if(currentName.includes(".")){
			currentName = currentName.substr(currentName.lastIndexOf(".")+1);
		}

		link.appendChild(document.createTextNode("> " + currentName + " ["+ typeName + "]"));

		const htmlEltParent = this.htmlAttributeElt;

		var enableFilter = true;


		// Fin du formulaire

		if(enableFilter && (subEltType.includes("DataObjectReference") || subEltType.includes("ContactElement")) ){
			link.onclick = function(){

				getJsonObjectFromServer("ResqmlAccessibleDOR?uuid="+constThis.rootUUID+"&subParamPath="+constThis.name+"&subParamName="+currentName).then(function(jsonContent){
					const modalID = "modalDOR_" + constThis.rootUUID; // modal window id to open

					// Debut du formulaire 

					/** Formulaire de creation par un uuid externe **/
					const formCreate_Empty = document.createElement("form");
					formCreate_Empty.action = "ObjectEdit";
					formCreate_Empty.method = "post";

					var inputCommand_Empty = document.createElement("input");
					inputCommand_Empty.type ="text";
					inputCommand_Empty.name = "command";
					inputCommand_Empty.value = "update";
					inputCommand_Empty.hidden = "hidden";
					formCreate_Empty.appendChild(inputCommand_Empty);

					var inputRootUUID = document.createElement("input");
					inputRootUUID.type ="text";
					inputRootUUID.name = "Root_UUID";
					inputRootUUID.value = constThis.rootUUID;
					inputRootUUID.hidden = "hidden";
					formCreate_Empty.appendChild(inputRootUUID);

					var inputUUID_Empty = document.createElement("input");
					inputUUID_Empty.type ="text";
					inputUUID_Empty.name = constSubEltName;
					inputUUID_Empty.className = "form-control";
					inputUUID_Empty.placeholder = "External UUID (e.g. 00000000-0000-0000-0000-000000000000)";
					inputUUID_Empty.style.minWidth = "600px";
					formCreate_Empty.appendChild(inputUUID_Empty);


					var inputSubmit_Empty = document.createElement("button");
					inputSubmit_Empty.className = "btn btn-success";
					inputSubmit_Empty.appendChild(document.createTextNode("OK"));
					inputSubmit_Empty.onclick = function(){
						sendPostForm_Func(formCreate_Empty, "ObjectEdit", function(){
																		constThis.refresh();
																	});
						closeModal(modalID);
					};

					/*var formDiv_Empty = document.createElement("div");
					//formDiv_Empty.className = "dorLinkerForm";
					formDiv_Empty.appendChild(formCreate_Empty);
					formDiv_Empty.appendChild(inputSubmit_Empty);*/

					var formDiv_Empty = createInputGroup([formCreate_Empty,inputSubmit_Empty], [false, true]);


					/** Formulaire de creation par un uuid interne e l'epc **/
					const formCreate = document.createElement("form");
					formCreate.action = "ObjectEdit";
					formCreate.method = "post";

					var inputCommand = document.createElement("input");
					inputCommand.type ="text";
					inputCommand.name = "command";
					inputCommand.value = "update";
					inputCommand.hidden = "hidden";
					formCreate.appendChild(inputCommand);

					var inputRootUUID = document.createElement("input");
					inputRootUUID.type ="text";
					inputRootUUID.name = "Root_UUID";
					inputRootUUID.value = constThis.rootUUID;
					inputRootUUID.hidden = "hidden";
					formCreate.appendChild(inputRootUUID);

					var tableDOR = createTableFromData(
							jsonContent, 
							["num", "type", "title", "uuid", "schemaVersion"], 
							["Num", "Type", "Title", "UUID", "SchemaVersion"], 
							jsonContent.map(contentElt => [ function(){openResqmlObjectContentByUUID(contentElt['uuid'])} ]), 
							null );

					var shouldBeCheckable = currentElt.type.includes("proposal2_2.Collection");

					for(var attIdx in currentElt.attributes){
						var attrib = currentElt.attributes[attIdx];
						if(attrib.name == subEltName){
							if(attrib.type.endsWith("List")){
								shouldBeCheckable = true;
							}
							break;
						}
					}

					/*console.log('sub elt name' + subEltName + " -- ")
					console.log(currentElt)*/
					if(shouldBeCheckable){
						transformTabToFormCheckable(tableDOR, jsonContent.map(contentElt => contentElt.uuid), constSubEltName);
					}else{
						transformTabToFormRadio(tableDOR, jsonContent.map(contentElt => contentElt.uuid), constSubEltName);
					}

					formCreate.appendChild(tableDOR);

					var inputSubmit = document.createElement("button");
					inputSubmit.className = "btn btn-success";
					inputSubmit.appendChild(document.createTextNode("OK"));
					inputSubmit.onclick = function(){
						sendPostForm_Func(formCreate, "ObjectEdit", function(){
																		constThis.refresh();
																	});
						closeModal(modalID);
					};

					var formDiv = document.createElement("div");
					formDiv.className = "dorLinkerForm";
					formDiv.appendChild(formCreate);

					var modalContentDiv = document.createElement("div");

					var label_Empty = document.createElement("label");
					label_Empty.append(document.createTextNode("External reference"));
					var label_Internal = document.createElement("label");
					label_Internal.append(document.createTextNode("Internal reference"));

					modalContentDiv.appendChild(createTableFilterInput(tableDOR));
					modalContentDiv.appendChild(label_Empty);
					modalContentDiv.appendChild(formDiv_Empty);
					modalContentDiv.appendChild(label_Internal);
					modalContentDiv.appendChild(formDiv);
					modalContentDiv.appendChild(inputSubmit);

					openModal(modalID, "Creating DOR for " + constThis.rootUUID, modalContentDiv);
				});
			};
		}else{
			// Debut du formulaire 

			const formCreate = document.createElement("form");
			formCreate.action = "ObjectEdit";
			formCreate.method = "post";
			formCreate.acceptCharset = "UTF-8";

			var inputCommand = document.createElement("input");
			inputCommand.name = "command";
			inputCommand.value = "update";
			inputCommand.hidden = "hidden";
			formCreate.appendChild(inputCommand);

			var inputUUID = document.createElement("input");
			inputUUID.name = "Root_UUID";
			inputUUID.value = this.rootUUID;
			inputUUID.hidden = "hidden";
			formCreate.appendChild(inputUUID);
					
			var inputPath = document.createElement("input");
			inputPath.name = "path";
			inputPath.value = constSubEltName;
			formCreate.appendChild(inputPath);

			inputCommand.value = "create";

			var inputTypeToCreate = document.createElement("input");
			inputTypeToCreate.name = "type";
			inputTypeToCreate.value = subEltType;
			formCreate.appendChild(inputTypeToCreate);

			// ATTENTION ! on ne supporte pas les CustomData
			if(!enableFilter || !subEltType.includes("CustomData")){			
				link.onclick = async function(){

					// We should save before creating the sub element, else, it will reset all modifications
					try{
						saveResqmlObjectByUUID(constThis.rootUUID)[0].then(
							function(){
								sendPostForm_Func(formCreate, "ObjectEdit", function(){
								constThis.refresh();
								})
							}
						);
					}catch(Except){
						console.log(Except);
						console.log("Exception while trying to save data before adding sub element");
						sendPostForm_Func(formCreate, "ObjectEdit", function(){
							constThis.refresh();
						});
					}
				};
			}else{
				link.className = "disabledLink";
			}
		}
		return link;
	}

	updateListAttributesNames(){
		if(this.isResqmlListType()){
			this.attributes.forEach(function(x){
				var newIdx = getHtmlEltIdxInParent(x.htmlAttributeElt);
				x.name = x.name.substring(0, x.name.lastIndexOf(".")+1) + newIdx;
				// console.log("new name : " + x.name);
			});
		}
	}


	create_activityView(){
		const constThis = this;
		var isUpdating = false;


		// Nom a afficher : la derniere partie du path
		var shortName = this.name;
		if(shortName.includes("."))
			shortName = shortName.substring(shortName.lastIndexOf('.')+1);

		var shortType = this.type;
		if(shortType.includes("."))
			shortType = shortType.substring(shortType.lastIndexOf('.')+1);

		/*
			   _____ _        _            __                                                     _      __
			  / ___/(_)  ____( )___  _____/ /_   __  ______  ___     ____  _________  ____  _____(_)__  / /____
			  \__ \/ /  / ___/// _ \/ ___/ __/  / / / / __ \/ _ \   / __ \/ ___/ __ \/ __ \/ ___/ / _ \/ __/ _ \
			 ___/ / /  / /__  /  __(__  ) /_   / /_/ / / / /  __/  / /_/ / /  / /_/ / /_/ / /  / /  __/ /_/  __/
			/____/_/   \___/  \___/____/\__/   \__,_/_/ /_/\___/  / .___/_/   \____/ .___/_/  /_/\___/\__/\___/
			                                                     /_/              /_/
		*/
		if(!this.isAttribute){	

			this.htmlAttributeElt.appendChild(col0);
		}else{
		/*
			   _____ _        _            __                         ____           _ ____            __        ___             __                  __      __  __       _ __          __  _
			  / ___/(_)  ____( )___  _____/ /_   __  ______  ___     / __/__  __  __(_) / /__     ____/ /__     / ( )____ ______/ /_  ________     _/_/___ _/ /_/ /______(_) /_  __  __/ /_| |
			  \__ \/ /  / ___/// _ \/ ___/ __/  / / / / __ \/ _ \   / /_/ _ \/ / / / / / / _ \   / __  / _ \   / /|// __ `/ ___/ __ \/ ___/ _ \   / // __ `/ __/ __/ ___/ / __ \/ / / / __// /
			 ___/ / /  / /__  /  __(__  ) /_   / /_/ / / / /  __/  / __/  __/ /_/ / / / /  __/  / /_/ /  __/  / /  / /_/ / /  / /_/ / /  /  __/  / // /_/ / /_/ /_/ /  / / /_/ / /_/ / /_ / /
			/____/_/   \___/  \___/____/\__/   \__,_/_/ /_/\___/  /_/  \___/\__,_/_/_/_/\___/   \__,_/\___/  /_/   \__,_/_/  /_.___/_/   \___/  / / \__,_/\__/\__/_/  /_/_.___/\__,_/\__//_/
			                                                                                                                                    |_|                                    /_/
		*/
			// Creation d'une div pour l'element courant et ses attributs
			if(this.htmlAttributeElt == null){	// On crée l'element si il n'existe pas deja
				this.htmlAttributeElt = document.createElement("div");
			}else{
				// Si existe deja on fait une mise à jour
				isUpdating = true;
				if(this.isResqmlListType() && (this.attributes == null || this.attributes.length <= 0) ){
					// Si c'est une liste et qu'elle est vide, on supprime l'element
					// this.remove();
					return null;
				}
			}
			this.htmlAttributeElt.name = ResqmlElement_NAMESUBATTPREFIX + "_" + shortName;

			

			if(!isUpdating && this.parentElt == null){
				this.subAttribCollapserElt.click();
			}
		}

		return this.htmlAttributeElt;
	}
}