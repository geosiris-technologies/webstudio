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
package com.geosiris.webstudio.etp;

import Energistics.Etp.v12.Datatypes.DataArrayTypes.DataArray;
import Energistics.Etp.v12.Datatypes.DataArrayTypes.DataArrayIdentifier;
import Energistics.Etp.v12.Datatypes.Object.ContextScopeKind;
import Energistics.Etp.v12.Protocol.DataArray.GetDataArrays;
import com.geosiris.energyml.exception.ObjectNotFoundNotError;
import com.geosiris.energyml.pkg.EPCFile;
import com.geosiris.energyml.pkg.EPCPackage;
import com.geosiris.energyml.utils.EnergymlWorkspace;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.etp.utils.ETPHelper;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.servlet.Editor;
import com.google.gson.Gson;
import jakarta.xml.bind.JAXBElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.geosiris.energyml.utils.EPCGenericManager.getObjectTypeForFilePath_fromClassName;
import static com.geosiris.energyml.utils.EnergymlWorkspaceHelper.getHdfReference;

public class ETPWorkspace implements EnergymlWorkspace {
    private static final Logger logger = LoggerFactory.getLogger(ETPWorkspace.class);

    private ETPClient client;
    private String dataspace;

    private HashMap<String, CacheData<String>> uuidUri_cache = new HashMap<>();

    private static HashMap<String, CacheData<String>> xml_cache = new HashMap<>();

    public class CacheData<T>{

        public static final long TIMEOUT = 1000*60*10; // 10 min
        public T data;
        public long epoch;

        public CacheData(T data){
            this.data = data;
            this.epoch = System.currentTimeMillis();
        }

        public boolean outDated(){
            return System.currentTimeMillis() - this.epoch > TIMEOUT;
        }
    }

    public ETPWorkspace(String dataspace, ETPClient client) {
        this.dataspace = dataspace;
        this.client = client;
        this.uuidUri_cache = new HashMap<>();
        updateUuidCache();
    }

    public String getDataspace() {
        return dataspace;
    }

    @Override
    public Object getObject(String uuid, String objectVersion) {
        return getObjectByIdentifier(EPCFile.getIdentifier(uuid, objectVersion));
    }

    @Override
    public Object getObjectByIdentifier(String identifier) {
        if(identifier.startsWith("eml:///")){
            return getEnergisticsObject(identifier);
        }else{
            String uuid = EPCFile.getUuidFromIdentifier(identifier);
            return getObjectByUUID(uuid);
        }
    }

    @Override
    public Object getObjectByUUID(String uuid) {
        return getEnergisticsObject(getObjectUriFromUuid(uuid));
    }

    @Override
    public List<?> readExternalArray(Object energymlArray, Object rootObj, String pathInRoot) throws ObjectNotFoundNotError {
        String pathInExternal = getHdfReference(energymlArray).get(0);
        String uri = getUriFromObject(rootObj, dataspace).toString();
        /*try {
            GetDataArraysResponse resp = ETPHelperREST.getMultipleDataArrays(client, uri, List.of(pathInExternal));
            List<?> res = dataArraysToNumbers(resp.getDataArrays().entrySet().stream()
                    .sorted(Comparator.comparing(a -> a.getKey().toString()))
//					.sorted(Comparator.comparingInt(e -> Integer.getInteger(e.getKey().toString())))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));
//            logger.info("@readExternalArray   values {} ", res.subList(0,9));
            return res;
        } catch (Exception _ignore) {_ignore.printStackTrace();}*/
        logger.info("uri " + uri + " pathInExternal " + pathInExternal);
        String serverHost = client.getServerUri().getHost();
        if(serverHost.contains("geosiris.com")) {
            // Using our own table raw download with http
            try {
                Map<CharSequence, DataArrayIdentifier> map = new HashMap<>();
                map.put("0", DataArrayIdentifier.newBuilder().setUri(uri).setPathInResource(pathInExternal).build());

                List<?> res = new ArrayList<>();
                HttpPost send_req = new HttpPost("http://" + client.getServerUri().getAuthority() + (client.getServerUri().getPath() != null ? "/" + client.getServerUri().getPath() : "") + "/data-array/raw/get");
                send_req.addHeader("content-type", "application/json");
                StringEntity params = new StringEntity(GetDataArrays.newBuilder().setDataArrays(map).build().toString());
                send_req.setEntity(params);
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpResponse answer = httpClient.execute(send_req);
                Gson gson = new Gson();
                String content = new BufferedReader(
                        new InputStreamReader(answer.getEntity().getContent(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                res = gson.fromJson(content, ArrayList.class);
                logger.info(String.valueOf(res));
                return res;
            } catch (Exception _ignore) {
                _ignore.printStackTrace();
            }
        }else{
            try {
//            List<?> res = ETPHelper.sendGetDataArray_prettier(client, uri, pathInExternal, 50000, true);
                List<?> res = ETPHelper.getMultipleDataArrays(client, uri, Collections.singletonList(pathInExternal), 500000).values().iterator().next();
                return res;
            } catch (Exception _ignore) {_ignore.printStackTrace();}
        }
        return null;
    }

    public String getEnergisticsXML(String uri) {
        if(xml_cache.containsKey(uri) && !xml_cache.get(uri).outDated()){
            logger.info("using cache for {}", uri);
            return xml_cache.get(uri).data;
        }else {
            ETPUri etpUri = ETPUri.parse(uri);
            etpUri.setDataspace(dataspace);
            logger.info("@getEnergisticsXML > {}", uri);
            List<String> m = ETPHelper.sendGetDataObjects_pretty(client, Collections.singletonList(etpUri.toString()), "xml", 50000);
            if (!m.isEmpty()) {
                xml_cache.put(uri, new CacheData<>(m.get(0)));
                return m.get(0);
            }
            return null;
        }
    }

    public Object getEnergisticsObject(String uri) {
        String xml = getEnergisticsXML(uri);
        if(xml != null){
            JAXBElement<?> elt = Editor.pkgManager.unmarshal(xml);
            return elt.getValue();
        }
        return null;
    }

    public String getObjectUriFromUuid(String uuid){
        if(uuidUri_cache.containsKey(uuid)) {
            return uuidUri_cache.get(uuid).data;
        }else{
            logger.info("@getObjectUriFromUuid {} dataspace : {}", uuid, dataspace);
            List<String> uris = ETPHelper.sendGetRessources_pretty(client, new ETPUri(dataspace).toString(), 1, ContextScopeKind.self, 5000000);
            for (String uri : uris) {
                if (uri.contains(uuid)) {
                    uuidUri_cache.put(uuid, new CacheData<>(uri));
                    return uri;
                }
            }
        }
        return null;
    }

    public void updateUuidCache(){
        logger.info("@updateUuidCache");
        try {
            List<String> uris = ETPHelper.sendGetRessources_pretty(client, new ETPUri(dataspace).toString(), 1, ContextScopeKind.self, 5000000);
            for (String uri : uris) {
                ETPUri etpuri = ETPUri.parse(uri);
                uuidUri_cache.put(etpuri.getUuid(), new CacheData<>(uri));
            }
        }catch (Exception e){logger.error("{}", e);}
    }

    public static ETPUri getUriFromObject(Object obj, String dataspace){
        EPCPackage epc_pkg = Editor.pkgManager.getMatchingPackage(obj.getClass());
        ETPUri uri = new ETPUri();
        uri.setDataspace(dataspace);
        uri.setUuid((String) ObjectController.getObjectAttributeValue(obj, "uuid"));
        uri.setDomain(epc_pkg.getDomain());
        uri.setDomainVersion(epc_pkg.getVersionNum().replace(".", "").substring(0,2));
        uri.setObjectType(getObjectTypeForFilePath_fromClassName(obj.getClass().getName()));
        uri.setVersion((String) ObjectController.getObjectAttributeValue(obj, "version"));
        return uri;
    }


    public static List<?> dataArraysToNumbers(List<DataArray> das){
        return das.stream()
                .map(da -> {
                    try {
                        List<?> values = ((List<?>) ObjectController.getObjectAttributeValue(da.getData().getItem(), "values"));
                        if(values == null){
                            // cas of object filled by json from http and deserialized by Gson :
                            // [Energistics.Etp.v12.Datatypes.ArrayOfInt, {values=[48.0, ...] } ]
                            // this is translated by Gson as : [String = "Energistics.Etp.v12.Datatypes.ArrayOfInt", com.google.gson.internal.LinkedTreeMap = {'values': [[48.0, ...]]}]
                            values = (List<?>) ((com.google.gson.internal.LinkedTreeMap) ((List)da.getData().getItem()).get(1)).get("values");
                        }
//                            logger.info("@dataArraysToNumbers values {} ", values.subList(0,9));
                        return values;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .flatMap(List::stream).collect(Collectors.toList());
    }

}
