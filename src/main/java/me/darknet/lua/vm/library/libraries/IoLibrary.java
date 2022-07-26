package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.data.UserData;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.*;

import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class IoLibrary extends Library {

	private final UserDataValue stdin;
	private final UserDataValue stdout;
	private final UserDataValue stderr;

	public IoLibrary() {
		super("io", "io");
		stdin = new UserDataValue(newStdFile(FileDescriptor.in));
		stdout = new UserDataValue(newStdFile(FileDescriptor.out));
		stderr = new UserDataValue(newStdFile(FileDescriptor.err));
		fileMethods.set("read", newClosure(this::fread));
	}

	Table fileMethods = new Table();

	public UserData newFile() {
		UserData userData = new UserData();
		userData.setMetatable(fileMethods);
		return userData;
	}

	public UserData newStdFile(FileDescriptor file) {
		UserData userData = new UserData();
		userData.setMetatable(fileMethods);
		userData.setValue(file);
		return userData;
	}

	public Value openFile(UserData file, String name) {
		FileDescriptor fd;
		try {
			File f = new File(name);
			// open file for reading
			FileInputStream fis = new FileInputStream(f);
			fd = fis.getFD();
		} catch (Exception e) {
			return NilValue.NIL;
		}
		file.setValue(fd);
		return new UserDataValue(file);
	}

	public int lua_open(ExecutionContext ctx) {
		UserData file = newFile();
		String name = ctx.getRequired(0).asString();
		String mode = ctx.optionalString(1, "r");
		// mode is ignored because java doesn't use fopen
		ctx.push(openFile(file, name));
		return 1;
	}

	public int read(ExecutionContext ctx, FileDescriptor fd, int first) {
		int amount = 0;
		try {
			FileInputStream fis = new FileInputStream(fd);
			Scanner scanner = new Scanner(fis);
			int n = ctx.size();
			for (int i = first; i < n; i++) {
				Value v = ctx.get(i);
				if (v.getType() == Type.NUMBER) {
					long size = (long) v.asNumber();
					// read size bytes
					byte[] bytes = new byte[(int) size];
					int read = fis.read(bytes);
					ctx.push(new StringValue(read == -1 ? "" : new String(bytes)));
					amount++;
				} else {
					String s = v.asString();
					try {
						switch (s.charAt(1)) {
							case 'n' -> {
								// read number
								double read = scanner.nextDouble();
								ctx.push(new NumberValue(read));
								amount++;
							}
							case 'l' -> {
								String line = scanner.nextLine();
								ctx.push(new StringValue(line));
								amount++;
							}
							case 'a' -> {
								// read the entire file
								StringBuilder buffer = new StringBuilder();
								byte[] bytes = new byte[1024];
								do {
									int read = fis.read(bytes);
									if (read == -1) {
										break;
									}
									buffer.append(new String(bytes, 0, read));
								} while (true);
								ctx.push(new StringValue(buffer.toString()));
								amount++;
							}
							default -> ctx.throwError("argument error #%d invalid format", i + 1 - first);
						}
					} catch (InputMismatchException e) {
						ctx.push(NilValue.NIL);
						amount++;
						break;
					}
				}
			}
		} catch (IOException e) {
			ctx.push(NilValue.NIL);
			ctx.push(new StringValue(e.getMessage()));
			ctx.push(new NumberValue(1));
			return 3;
		}
		return amount;
	}

	public FileDescriptor toFd(ExecutionContext ctx, Value v) {
		if (v instanceof UserDataValue uv) {
			UserData userData = uv.getValue();
			Object value = userData.getValue();
			if (value == null) {
				ctx.throwError("attempt to use a closed file");
			}
			return (FileDescriptor) value;
		} else {
			ctx.throwError("bad argument #1 to 'read' (file expected, got %s)", v.getType());
		}
		return null;
	}

	public int lua_read(ExecutionContext ctx) {
		return read(ctx, toFd(ctx, stdin), 0);
	}

	public int fread(ExecutionContext ctx) {
		return read(ctx, toFd(ctx, ctx.getRequired(0)), 1);
	}

	public int write(ExecutionContext ctx, FileDescriptor fd, int argument) {
		int numargs = ctx.size();
		int status = 0;
		try {
			FileOutputStream fos = new FileOutputStream(fd); // do not close this stream
			for (; numargs-- > 0; argument++) {
				fos.write(ctx.get(argument).asString().getBytes());
			}
		} catch (IOException e) {
			status = -1;
			e.printStackTrace();
		}
		ctx.push(new BooleanValue(status == 0));
		return 1;
	}

	public int lua_write(ExecutionContext ctx) {
		return write(ctx, toFd(ctx, stdout), 0);
	}

	public int fwrite(ExecutionContext ctx) {
		return write(ctx, toFd(ctx, ctx.getRequired(0)), 1);
	}

}
