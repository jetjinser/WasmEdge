package org.wasmedge;

import org.wasmedge.enums.HostRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WasmEdgeVM {
    public static final Map<String, HostFunction> funcMap = new HashMap<>();
    private static final Map<String, Object> externRefMap = new HashMap<>();
    private final ConfigureContext configureContext;
    private final StoreContext storeContext;
    private long pointer;


    public WasmEdgeVM(ConfigureContext configureContext, StoreContext storeContext) {
        this.configureContext = configureContext;
        this.storeContext = storeContext;
        nativeInit(this.configureContext, this.storeContext);
    }

    protected static void addExternRef(String key, Object val) {
        externRefMap.put(key, val);
    }

    protected static Object getExternRef(String key) {
        return externRefMap.get(key);
    }

    protected static String addHostFunc(HostFunction hostFunction) {
        String key = UUID.randomUUID().toString();
        funcMap.put(key, hostFunction);
        return key;
    }

    protected static HostFunction getHostFunc(String key) {
        return funcMap.get(key);
    }

    private native void nativeInit(ConfigureContext configureContext, StoreContext storeContext);

    private native void runWasmFromFile(String file,
                                        String funcName,
                                        Value[] params,
                                        int paramSize,
                                        int[] paramTypes,
                                        Value[] returns,
                                        int returnSize,
                                        int[] returnTypes
    );

    /**
     * Run a wasm file.
     *
     * @param file     file path.
     * @param funcName function name to run.
     * @param params   params for the function.
     * @param returns  return values.
     */
    public void runWasmFromFile(String file,
                                String funcName,
                                List<Value> params,
                                List<Value> returns) {
        Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);

        Value[] returnsArray = valueListToArray(returns);
        int[] returnTypes = getValueTypeArray(returns);

        runWasmFromFile(file, funcName, paramsArray, params.size(), paramTypes,
                returnsArray, returns.size(), returnTypes);
    }

    public void runWasmFromBuffer(byte[] buffer, String funcName, List<Value> params, List<Value> returns) {
        Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);

        Value[] returnsArray = valueListToArray(returns);
        int[] returnTypes = getValueTypeArray(returns);

        runWasmFromBuffer(buffer, funcName, paramsArray, paramTypes, returnsArray, returnTypes);
    }

    private native void runWasmFromBuffer(byte[] buffer, String funcName, Value[] params, int[] paramTypes, Value[] returns, int[] returnTypes);


    public void runWasmFromASTModule(ASTModuleContext astModuleContext, String funcName, List<Value> params, List<Value> returns) {
        Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);

        Value[] returnsArray = valueListToArray(returns);
        int[] returnTypes = getValueTypeArray(returns);

        runWasmFromASTModule(astModuleContext, funcName, paramsArray, paramTypes, returnsArray, returnTypes);
    }

    private native void runWasmFromASTModule(ASTModuleContext astModuleContext, String funcName, Value[] params, int[] paramTypes, Value[] returns, int[] returnTypes);


    private int[] getValueTypeArray(List<Value> values) {

        int[] types = new int[values.size()];

        for (int i = 0; i < values.size(); i++) {
            types[i] = values.get(i).getType().ordinal();
        }
        return types;
    }

    private Value[] valueListToArray(List<Value> values) {
        Value[] valuesArray = new Value[values.size()];
        values.toArray(valuesArray);
        return valuesArray;
    }

    public native void loadWasmFromFile(String filePath);

    public native void loadWasmFromBuffer(byte[] buffer);

    public native void loadWasmFromASTModule(ASTModuleContext astModuleContext);

    public native void validate();

    public native void instantiate();

    public void execute(String funcName, List<Value> params, List<Value> returns) {
        Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);

        Value[] returnsArray = valueListToArray(returns);
        int[] returnTypes = getValueTypeArray(returns);

        execute(funcName, paramsArray, params.size(), paramTypes, returnsArray, returns.size(), returnTypes);
    }

    public native void execute(String funcName, Value[] params,
                               int paramSize,
                               int[] paramTypes,
                               Value[] returns,
                               int returnSize,
                               int[] returnTypes);


    public void destroy() {
        if (configureContext != null) {
            configureContext.destroy();
        }

        if (storeContext != null) {
            storeContext.destroy();
        }
        delete();
        this.pointer = 0;
    }

    public native void registerModuleFromFile(String modName, String filePath);

    public native void registerModuleFromBuffer(String moduleName, byte[] buffer);

    public native void registerModuleFromImport(ModuleInstanceContext moduleInstanceContext);

    public native void registerModuleFromASTModule(String moduleName, ASTModuleContext astModuleContext);

    public void executeRegistered(String modName, String funcName, List<Value> params,
                                  List<Value> returns) {
        Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);

        Value[] returnsArray = valueListToArray(returns);
        int[] returnTypes = getValueTypeArray(returns);
        executeRegistered(modName, funcName, paramsArray, paramTypes, returnsArray, returnTypes);
    }

    private native void executeRegistered(String modName, String funcName, Value[] params,
                                          int[] paramTypes, Value[] returns, int[] returnTypes);

    private native void getFunctionList(List<FunctionTypeContext> functionList);

    public List<FunctionTypeContext> getFunctionList() {
        List<FunctionTypeContext> funcList = new ArrayList<>();
        getFunctionList(funcList);
        return funcList;
    }

    public native FunctionTypeContext getFunctionType(String funcName);

    public ModuleInstanceContext getImportModuleContext(HostRegistration reg) {
        return nativeGetImportModuleContext(reg.getVal());
    }

    private native ModuleInstanceContext nativeGetImportModuleContext(int reg);


    public native StoreContext getStoreContext();

    public native StatisticsContext getStatisticsContext();

    public native FunctionTypeContext getFunctionTypeRegistered(String moduleName,
                                                                String funcName);

    public native void cleanUp();

    private native void delete();

    // Async API
    private native WasmEdgeAsync asyncRunWasmFromFile(
            String path, String funcName,
            Value[] params, int[] paramTypes);

    public WasmEdgeAsync asyncRunWasmFromFile(
            String path, String funcName,
            List<Value> params){
                Value[] paramsArray = valueListToArray(params);
                int[] paramTypes = getValueTypeArray(params);

                return asyncRunWasmFromFile(path, funcName, paramsArray, paramTypes);
            }

    private native WasmEdgeAsync asyncRunWasmFromBuffer(
            byte[] buffer,
            String funcName, Value[] params,
            int[] paramTypes);

    public WasmEdgeAsync asyncRunWasmFromBuffer(
            byte[] buffer,
            String funcName, List<Value> params){
                Value[] paramsArray = valueListToArray(params);
                int[] paramTypes = getValueTypeArray(params);

                return asyncRunWasmFromBuffer(buffer, funcName, paramsArray, paramTypes);
            }
            
    private native WasmEdgeAsync asyncRunWasmFromASTModule(
            ASTModuleContext astCxt,
            String funcName, Value[] params,
            int[] paramTypes);

    public WasmEdgeAsync asyncRunWasmFromASTModule(
            ASTModuleContext astCxt,
            String funcName, List<Value> params){
                Value[] paramsArray = valueListToArray(params);
                int[] paramTypes = getValueTypeArray(params);

                return asyncRunWasmFromASTModule(astCxt, funcName, paramsArray, paramTypes);
            }

    private native WasmEdgeAsync
    asyncExecute(String funcName,
                 Value[] params, int[] paramTypes);

    public WasmEdgeAsync
    asyncExecute(String funcName, List<Value> params){
         Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);
        return asyncExecute(funcName, paramsArray, paramTypes);
                            }

    private native WasmEdgeAsync asyncExecuteRegistered(
            String moduleName,
            String funcName, Value[] params,
            int[] paramTypes);

    public WasmEdgeAsync asyncExecuteRegistered(
            String moduleName,
            String funcName, List<Value> params
            ){
                Value[] paramsArray = valueListToArray(params);
        int[] paramTypes = getValueTypeArray(params);

        return asyncExecuteRegistered(moduleName, funcName, paramsArray, paramTypes);
            }
}
