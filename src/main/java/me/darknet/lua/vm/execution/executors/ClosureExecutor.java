package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.ClosureInstruction;
import me.darknet.lua.file.instructions.GetUpvalueInstruction;
import me.darknet.lua.file.instructions.Instruction;
import me.darknet.lua.file.instructions.MoveInstruction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.util.ValueUtil;
import me.darknet.lua.vm.value.ClosureValue;
import me.darknet.lua.vm.value.Value;

public class ClosureExecutor implements Executor<ClosureInstruction> {
	@Override
	public void execute(ClosureInstruction inst, ExecutionContext ctx) {
		LuaFunction ci = inst.getFunction();
		int numUps = ci.getUpvalues().size();
		Closure cl = new Closure(ci, ctx.getEnv());
		// set upvalues
		// this process might seem like a hack, but it is how the lua VM actually does it
		// so the upvalues are set via 'instructions' instead of directly in the closure,
		// so we iterate over the next instructions and set the upvalues accordingly
		ctx.set(inst.getRegister(), new ClosureValue(cl)); // preset the closure in the register so upvalues can reference it
		ctx.incPc();
		for (int i = 0; i < numUps; i++) {
			// get next instruction
			Instruction next = ctx.nextInstruction();
			Value toCopy;
			if (next instanceof GetUpvalueInstruction gi) {
				toCopy = ctx.getClosure().getUpvalue(gi.getUpvalue());
			} else if (next instanceof MoveInstruction mi) {
				toCopy = ctx.get(mi.getFrom());
			} else {
				throw new IllegalStateException("Unexpected instruction: " + next);
			}
			// now we need to copy the value to avoid external changes to the value
			cl.setUpvalue(i, ValueUtil.clone(toCopy));

		}
		ctx.setPc(ctx.getPc() - 1);
	}
}
