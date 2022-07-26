package me.darknet.lua.file.function;

import me.darknet.lua.file.LuaDataStream;
import me.darknet.lua.file.LuaFile;
import me.darknet.lua.file.constant.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuaFunctionReader implements ConstantTypes {

	LuaFile luaFile;
	LuaDataStream stream;
	int version;

	public LuaFunctionReader(LuaFile luaFile, LuaDataStream stream) {
		this.luaFile = luaFile;
		this.stream = stream;
		this.version = luaFile.getVersion();
	}

	public void readCode(LuaFunction function) throws IOException {
		int codeSize = stream.readInteger();
		if (codeSize < 0) {
			function.setCode(new int[0]);
			return;
		}
		int[] code = new int[codeSize];
		for (int i = 0; i < codeSize; i++) {
			code[i] = stream.readInteger();
		}
		function.setCode(code);
	}

	public void readLines(LuaFunction function) throws IOException {
		int n = stream.readInteger();
		for (int i = 0; i < n; i++) {
			function.addLine(stream.readInteger());
		}
	}

	public void readLocals(LuaFunction function) throws IOException {
		int localCount = stream.readInteger();
		for (int i = 0; i < localCount; i++) {
			Local local = new Local(stream.readString(), stream.readInteger(), stream.readInteger());
			function.addLocal(local);
		}
	}

	public void readUpvalues(LuaFunction function, boolean readKind, boolean onlyNames) throws IOException {
		int upvalueCount = stream.readInteger();
		for (int i = 0; i < upvalueCount; i++) {
			Upvalue upvalue;
			if (onlyNames) {
				upvalue = new Upvalue(stream.readString(), false, 0, 0);
			} else {
				upvalue = new Upvalue(
						stream.readString(),
						stream.readByte() != 0,
						stream.readByte(),
						readKind ? stream.readByte() : 0);
			}
			function.addUpvalue(upvalue);
		}
	}

	public void readConstants(LuaFunction function) throws IOException {
		List<Constant> constants = new ArrayList<>();
		int constantCount = stream.readInteger();
		for (int i = 0; i < constantCount; i++) {
			int type = stream.readByte();
			constants.add(switch (type) {
				case TNIL -> new NilConstant();
				case TBOOLEAN -> new BooleanConstant(stream.readByte() != 0);
				case VTRUE -> new BooleanConstant(true);
				case TNUMBER -> {
					if (version >= 83) yield new IntConstant(stream.readInteger());
					else yield new NumberConstant(stream.readNumber());
				}
				case TSTRING, VLNGSTR -> new StringConstant(stream.readString());
				default -> throw new IllegalStateException("Unexpected value: " + type);
			});
		}
		function.setConstants(constants);
	}

	public void readProtos(LuaFunction function) throws IOException {
		int protoCount = stream.readInteger();
		for (int i = 0; i < protoCount; i++) {
			function.addPrototype(readFunction());
		}
	}

	public LuaFunction readFunction() throws IOException {
		LuaFunction function = new LuaFunction();
		if (version != 82) function.setSource(stream.readString());
		function.setLineDefined(stream.readInteger());
		if (version > 80) function.setLastLineDefined(stream.readInteger());
		if (version < 82) function.setNumUps(stream.readByte());
		function.setNumParams(stream.readByte());
		function.setIsVararg(stream.readByte());
		function.setMaxStackSize(stream.readByte());

		if (version > 80) {
			readCode(function);
			readConstants(function);
			if (version > 82) {
				readProtos(function);
			} else {
				readProtos(function);
			}
		}

		if (version == 82) function.setSource(stream.readString());
		readLines(function);
		readLocals(function);
		readUpvalues(function, false, true);

		if (version == 80) {
			readConstants(function);
			readProtos(function);
			readCode(function);
		}

		LuaInstructionReader insnReader = new LuaInstructionReader(function);
		function.setInstructions(insnReader.read());

		return function;

	}

}
