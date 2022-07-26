package me.darknet.lua.file.instructions;

/*
 * Lua 5.0 Opcode standard
 */
public interface Opcodes {

static int MASK1(int n, int p) {
	return ((~((~(int)0)<<n))<<p);
}

int SIZE_C =  		9;
int SIZE_B =        9;
int SIZE_Bx =       (SIZE_C + SIZE_B);
int SIZE_A =        8;
int SIZE_OP =       6;
int POS_OP =        0;
int POS_A =         (POS_OP + SIZE_OP);
int POS_C =         (POS_A + SIZE_A); // 9 bits
int POS_B =         (POS_C + SIZE_C);
int POS_Bx =        POS_C; // 18 bits
int BITK = 			(1 << (SIZE_B - 1));
int MAXARG_Bx = 	((1 << SIZE_Bx) - 1);
int MAXARG_sBx = 	(MAXARG_Bx >> 1);
int MAXARG_A =      ((1<<SIZE_A)-1);
int MAXARG_B =      ((1<<SIZE_B)-1);
int MAXARG_C =      ((1<<SIZE_C)-1);

static int getOpcode(int i) {
	return i >> POS_OP & MASK1(SIZE_OP, 0);
}

static int getArgA(int i) {
	return i >> POS_A & MASK1(SIZE_A, 0);
}

static int getArgB(int i) {
	return i >> POS_B & MASK1(SIZE_B, 0);
}

static int getArgC(int i) {
	return i >> POS_C & MASK1(SIZE_C, 0);
}

static int getArgBx(int i) {
	return i >> POS_Bx & MASK1(SIZE_Bx, 0);
}

static int getArgsBx(int i) {
	// make Bx signed
	return getArgBx(i) - MAXARG_sBx;
}

static boolean isK(int i) {
	return (i & BITK) != 0;
}

static int getK(int i) {
	return i & ~BITK;
}

// R(A) := ?
int MOVE = 		0x00; // A B | R(A) := R(B)
int LOADK = 	0x01; // A Bx | R(A) := Kst(Bx)
int LOADBOOL = 	0x02; // A B C | R(A) := (Bool)B; if (C) pc++
int LOADNIL = 	0x03; // A B | R(A) := ...
int GETUPVAL = 	0x04; // A B | R(A) := UpValue[B]
int GETGLOBAL = 0x05; // A Bx | R(A) := Gbl[Kst(Bx)]
int GETTABLE = 	0x06; // A B C | R(A) := R(B)[R(C)]

// ? = R(A)
int SETGLOBAL = 0x07; // A Bx | Gbl[Kst(Bx)] := R(A)
int SETUPVAL = 	0x08; // A B | UpValue[B] := R(A) (unused)
int SETTABLE = 	0x09; // A B C | R(A)[RK(B)] := RK(C)

int NEWTABLE = 	0x0A; // A B C | R(A) := {} (size = B,C)

int SELF = 		0x0B; // A B C | R(A+1) := R(B); R(A) := R(B)[R(C)]
// ARITHMETIC R(A) := R(B) op R(C)
int ADD = 		0x0C; // A B C | R(A) := R(B) + R(C)
int SUB = 		0x0D; // A B C | R(A) := R(B) - R(C)
int MUL = 		0x0E; // A B C | R(A) := R(B) * R(C)
int DIV = 		0x0F; // A B C | R(A) := R(B) / R(C)
int MOD = 		0x10; // A B C | R(A) := R(B) % R(C)
int POW = 		0x11; // A B C | R(A) := R(B) ^ R(C)
int UNM = 		0x12; // A B | R(A) := -R(B)
int NOT = 		0x13; // A B | R(A) := not R(B)
int LEN = 		0x14; // A B | R(A) := length of R(B)

int CONCAT = 	0x15; // A B C | R(A) := R(B).. ... ..R(C)

// CONTROL FLOW
int JMP = 		0x16; // sBx | pc+=sBx
int EQ = 		0x17; // A B C | if ((RK(B) == RK(C)) ~= A) then pc++
int LT = 		0x18; // A B C | if ((RK(B) < RK(C)) ~= A) then pc++
int LE = 		0x19; // A B C | if ((RK(B) <= RK(C)) ~= A) then pc++

int TEST = 		0x1A; // A C | if not (R(A) <=> C) then pc++
int TESTSET = 	0x1B; // A B C | if (R(B) <=> C) then R(A) := R(B) else pc++

int CALL = 		0x1C; // A B C | R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
int TAILCALL = 	0x1D; // A B C | return R(A)(R(A+1), ... ,R(A+B-1))
int RETURN = 	0x1E; // A B | return R(A), ... ,R(A+B-2)

int FORLOOP = 	0x1F; // A sBx | R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }
int FORPREP = 	0x20; // A sBx | R(A)-=R(A+2); pc+=sBx
int TFORLOOP = 	0x21; // A C | if R(A+1) ~= nil then { R(A)=R(A+1); pc+=sBx } else R(A+3)=nil

int SETLIST = 	0x22; // A B C | R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B

int CLOSE = 	0x23; // A | close all variables in the stack up to (>=) R(A)
int CLOSURE = 	0x24; // A Bx | R(A) := closure(KPROTO[Bx])

int VARARG = 	0x25; // A B | R(A), R(A+1), ..., R(A+B-2) = vararg

String[] OPCODES = {
	"MOVE", "LOADK", "LOADBOOL", "LOADNIL", "GETUPVAL", "GETGLOBAL", "GETTABLE",
	"SETGLOBAL", "SETUPVAL", "SETTABLE", "NEWTABLE", "SELF", "ADD", "SUB", "MUL", "DIV", "MOD", "POW", "UNM", "NOT", "LEN",
	"CONCAT", "JMP", "EQ", "LT", "LE", "TEST", "TESTSET", "CALL", "TAILCALL", "RETURN", "FORLOOP", "FORPREP", "TFORLOOP",
	"SETLIST", "CLOSE", "CLOSURE", "VARARG"
};

// argument masks
int OpArgN = 0; // unused argument
int OpArgU = 1; // argument is used
int OpArgR = 2; // argument is register or jump offset
int OpArgK = 3; // argument is a constant or register/constant

// opcode modes
int iABC = 0; // use ABC format
int iABx = 1; // use A Bx format
int iAsBx = 2; // use A sBx format

static int opmode(int t, int a, int b, int c, int m) {
	return (((t)<<7) | ((a)<<6) | ((b)<<4) | ((c)<<2) | (m));
}

static int getOpMode(int op) {
	return MODES[op] & 3;
}

static boolean isAMode(int op) {
	return (MODES[op] & (1 << 6)) != 0;
}

static boolean isTMode(int op) {
	return (MODES[op] & (1 << 7)) != 0;
}

int[] MODES = {
/*       		 T  A    B       C     mode		   opcode	*/
		 opmode(0, 1, OpArgR, OpArgN, iABC) 		/* OP_MOVE */
		,opmode(0, 1, OpArgK, OpArgN, iABx)		/* OP_LOADK */
		,opmode(0, 1, OpArgU, OpArgU, iABC)		/* OP_LOADBOOL */
		,opmode(0, 1, OpArgR, OpArgN, iABC)		/* OP_LOADNIL */
		,opmode(0, 1, OpArgU, OpArgN, iABC)		/* OP_GETUPVAL */
		,opmode(0, 1, OpArgK, OpArgN, iABx)		/* OP_GETGLOBAL */
		,opmode(0, 1, OpArgR, OpArgK, iABC)		/* OP_GETTABLE */
		,opmode(0, 0, OpArgK, OpArgN, iABx)		/* OP_SETGLOBAL */
		,opmode(0, 0, OpArgU, OpArgN, iABC)		/* OP_SETUPVAL */
		,opmode(0, 0, OpArgK, OpArgK, iABC)		/* OP_SETTABLE */
		,opmode(0, 1, OpArgU, OpArgU, iABC)		/* OP_NEWTABLE */
		,opmode(0, 1, OpArgR, OpArgK, iABC)		/* OP_SELF */
		,opmode(0, 1, OpArgK, OpArgK, iABC)		/* OP_ADD */
		,opmode(0, 1, OpArgK, OpArgK, iABC)		/* OP_SUB */
		,opmode(0, 1, OpArgK, OpArgK, iABC)		/* OP_MUL */
		,opmode(0, 1, OpArgK, OpArgK, iABC)		/* OP_DIV */
		,opmode(0, 1, OpArgK, OpArgK, iABC)		/* OP_MOD */
		,opmode(0, 1, OpArgK, OpArgK, iABC)		/* OP_POW */
		,opmode(0, 1, OpArgR, OpArgN, iABC)		/* OP_UNM */
		,opmode(0, 1, OpArgR, OpArgN, iABC)		/* OP_NOT */
		,opmode(0, 1, OpArgR, OpArgN, iABC)		/* OP_LEN */
		,opmode(0, 1, OpArgR, OpArgR, iABC)		/* OP_CONCAT */
		,opmode(0, 0, OpArgR, OpArgN, iAsBx)		/* OP_JMP */
		,opmode(1, 0, OpArgK, OpArgK, iABC)		/* OP_EQ */
		,opmode(1, 0, OpArgK, OpArgK, iABC)		/* OP_LT */
		,opmode(1, 0, OpArgK, OpArgK, iABC)		/* OP_LE */
		,opmode(1, 1, OpArgR, OpArgU, iABC)		/* OP_TEST */
		,opmode(1, 1, OpArgR, OpArgU, iABC)		/* OP_TESTSET */
		,opmode(0, 1, OpArgU, OpArgU, iABC)		/* OP_CALL */
		,opmode(0, 1, OpArgU, OpArgU, iABC)		/* OP_TAILCALL */
		,opmode(0, 0, OpArgU, OpArgN, iABC)		/* OP_RETURN */
		,opmode(0, 1, OpArgR, OpArgN, iAsBx)		/* OP_FORLOOP */
		,opmode(0, 1, OpArgR, OpArgN, iAsBx)		/* OP_FORPREP */
		,opmode(1, 0, OpArgN, OpArgU, iABC)		/* OP_TFORLOOP */
		,opmode(0, 0, OpArgU, OpArgU, iABC)		/* OP_SETLIST */
		,opmode(0, 0, OpArgN, OpArgN, iABC)		/* OP_CLOSE */
		,opmode(0, 1, OpArgU, OpArgN, iABx)		/* OP_CLOSURE */
		,opmode(0, 1, OpArgU, OpArgN, iABC)		/* OP_VARARG */
};

}
