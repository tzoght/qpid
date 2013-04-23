/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
define(["dojo/dom",
        "dojo/_base/xhr",
        "dojo/parser",
        "dojo/query",
        "dojo/_base/connect",
        "dijit/registry",
        "qpid/common/properties",
        "qpid/common/updater",
        "qpid/common/util",
        "qpid/common/formatter",
        "qpid/management/addKeystore",
        "dojo/domReady!"],
       function (dom, xhr, parser, query, connect, registry, properties, updater, util, formatter, addKeystore) {

           function TrustStore(name, parent, controller) {
               this.keyStoreName = name;
               this.controller = controller;
               this.modelObj = { type: "truststore", name: name };
               if(parent) {
                   this.modelObj.parent = {};
                   this.modelObj.parent[ parent.type] = parent;
               }
               this.url = "rest/truststore/" + encodeURIComponent(name);
               this.dialog =  addKeystore.showTruststoreDialog;
           }

           TrustStore.prototype.getTitle = function() {
               return "TrustStore: " + this.keyStoreName;
           };

           TrustStore.prototype.open = function(contentPane) {
               var that = this;
               this.contentPane = contentPane;
               xhr.get({url: "showTrustStore.html",
                        sync: true,
                        load:  function(data) {
                            contentPane.containerNode.innerHTML = data;
                            parser.parse(contentPane.containerNode);

                            that.keyStoreUpdater = new KeyStoreUpdater(contentPane.containerNode, that.modelObj, that.controller, that.url);

                            updater.add( that.keyStoreUpdater );

                            that.keyStoreUpdater.update();

                            var deleteTrustStoreButton = query(".deleteTrustStoreButton", contentPane.containerNode)[0];
                            var node = registry.byNode(deleteTrustStoreButton);
                            connect.connect(node, "onClick",
                                function(evt){
                                    that.deleteKeyStore();
                                });

                            var editTrustStoreButton = query(".editTrustStoreButton", contentPane.containerNode)[0];
                            var node = registry.byNode(editTrustStoreButton);
                            connect.connect(node, "onClick",
                                function(evt){
                                  that.dialog(that.keyStoreUpdater.keyStoreData)
                                });
                        }});
           };

           TrustStore.prototype.close = function() {
               updater.remove( this.keyStoreUpdater );
           };

           function KeyStoreUpdater(containerNode, keyStoreObj, controller, url)
           {
               var that = this;

               function findNode(name) {
                   return query("." + name + "Value", containerNode)[0];
               }

               function storeNodes(names)
               {
                  for(var i = 0; i < names.length; i++) {
                      that[names[i]] = findNode(names[i]);
                  }
               }

               storeNodes(["name",
                           "path",
                           "type",
                           "trustManagerFactoryAlgorithm",
                           "certificateAlias",
                           "peersOnly"
                           ]);

               this.query = url;

               xhr.get({url: this.query, sync: properties.useSyncGet, handleAs: "json"}).then(function(data)
                               {
                                  that.keyStoreData = data[0];
                                  that.updateHeader();
                               });

           }

           KeyStoreUpdater.prototype.updateHeader = function()
           {
              this.name.innerHTML = this.keyStoreData[ "name" ];
              this.path.innerHTML = this.keyStoreData[ "path" ];
              this.type.innerHTML = this.keyStoreData[ "type" ];
              this.trustManagerFactoryAlgorithm.innerHTML = this.keyStoreData[ "trustManagerFactoryAlgorithm" ];
              this.peersOnly.innerHTML = "<input type='checkbox' disabled='disabled' "+(this.keyStoreData[ "peersOnly" ] ? "checked='checked'": "")+" />" ;
           };

           KeyStoreUpdater.prototype.update = function()
           {

              var thisObj = this;

              xhr.get({url: this.query, sync: properties.useSyncGet, handleAs: "json"}).then(function(data)
                   {
                      thisObj.keyStoreData = data[0];
                      thisObj.updateHeader();
                   });
           };

           TrustStore.prototype.deleteKeyStore = function() {
               if(confirm("Are you sure you want to delete trust store '" +this.keyStoreName+"'?")) {
                   var query = this.url;
                   this.success = true
                   var that = this;
                   xhr.del({url: query, sync: true, handleAs: "json"}).then(
                       function(data) {
                           that.contentPane.onClose()
                           that.controller.tabContainer.removeChild(that.contentPane);
                           that.contentPane.destroyRecursive();
                           that.close();
                       },
                       function(error) {that.success = false; that.failureReason = error;});
                   if(!this.success ) {
                       alert("Error:" + this.failureReason);
                   }
               }
           }

           return TrustStore;
       });
