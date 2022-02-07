package cal;

import org.cal.antlr.calBaseVisitor;
import org.cal.antlr.calParser;

public class IRGeneratorVisitor extends calBaseVisitor<String> {
    private int paramIdx = 1;
    private int jmpIdx = 1;
    private final QuadrupleQueue quadQueue = new QuadrupleQueue();

    @Override
    public String visitProgStm(calParser.ProgStmContext ctx) {
        String code = "";

        if (ctx.decl_list() != null)
            code += visit(ctx.decl_list()) + "\n";
        if (ctx.function_list() != null)
            code += visit(ctx.function_list()) + "\n";
        if (ctx.main() != null)
            code += visit(ctx.main());

        return code;
    }

    @Override
    public String visitDeclListStm(calParser.DeclListStmContext ctx) {
        String code = "";

        if (ctx.decl() != null)
            code += visit(ctx.decl());
        if (ctx.decl_list() != null)
            code += visit(ctx.decl_list());

        return code;
    }

    @Override
    public String visitType(calParser.TypeContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitEmptyStm(calParser.EmptyStmContext ctx) {
        return "";
    }

    @Override
    public String visitVarDeclRef(calParser.VarDeclRefContext ctx) {
        return "";
    }

    @Override
    public String visitVarDeclStm(calParser.VarDeclStmContext ctx) {
        return "";
    }

    @Override
    public String visitConstDeclRef(calParser.ConstDeclRefContext ctx) {
        return visit(ctx.const_decl());
    }

    @Override
    public String visitConstDeclStm(calParser.ConstDeclStmContext ctx) {
        return "\t" + ctx.ID().getText() + " = " + visit(ctx.expression()) + "\n";
    }

    @Override
    public String visitFuncListStm(calParser.FuncListStmContext ctx) {
        String code = "";

        if (ctx.function() != null)
            code += visit(ctx.function());
        if (ctx.function_list() != null)
            code += visit(ctx.function_list());

        return code;
    }

    @Override
    public String visitFuncDeclStm(calParser.FuncDeclStmContext ctx) {
        String code = "";
        this.paramIdx = 1;

        code += ctx.ID().getText() + ":\n";

        if (ctx.parameter_list() != null)
            code += visit(ctx.parameter_list());
        if (ctx.decl_list() != null)
            code += visit(ctx.decl_list());
        if (ctx.statement_block() != null)
            code += visit(ctx.statement_block());
        if (ctx.expression() != null)
            code += "\treturn " + visit(ctx.expression());

        return code;
    }

    @Override
    public String visitNonEmptyParamRef(calParser.NonEmptyParamRefContext ctx) {
        String code = "";
        if (ctx.nemp_paramerter_list() != null)
            code += visit(ctx.nemp_paramerter_list());
        return code;
    }

    @Override
    public String visitSingleParamStm(calParser.SingleParamStmContext ctx) {
        String line = "\t" + ctx.ID().getText() + " = getparam " + paramIdx + "\n";
        this.paramIdx++;
        return line;
    }

    @Override
    public String visitMultipleParamStm(calParser.MultipleParamStmContext ctx) {
        String code = "";
        if (ctx.ID() != null) {
            code += "\t" + ctx.ID().getText() + " = getparam " + paramIdx + "\n";
            paramIdx++;
        } if (ctx.nemp_paramerter_list() != null)
            code += visit(ctx.nemp_paramerter_list());
        return code;
    }

    @Override
    public String visitMainStm(calParser.MainStmContext ctx) {
        String code = "main:\n";
        if (ctx.decl_list() != null)
            code += visit(ctx.decl_list());
        code += "\n";
        if (ctx.statement_block() != null)
            code += visit(ctx.statement_block());
        code += "\tcall _exit, 0";
        return code;
    }

    @Override
    public String visitStmBlockRef(calParser.StmBlockRefContext ctx) {
        String code = "";
        if (ctx.statement() != null)
            code += visit(ctx.statement()) + "\n";
        if (ctx.statement_block() != null)
            code += visit(ctx.statement_block());
        return code;
    }

    @Override
    public String visitEmptyStatment(calParser.EmptyStatmentContext ctx) {
        return "";
    }

    @Override
    public String visitAssignStm(calParser.AssignStmContext ctx) {
        String code = visit(ctx.expression());
        quadQueue.clear();
        String[] lines = code.split("\n");
        lines[lines.length - 1] = "\t" + ctx.ID().getText() + " = " + lines[lines.length - 1] + "\n";

        if (code.contains("param"))
            return "\n" + String.join("\n", lines);

        code = "";
        quadQueue.add("=", visit(ctx.expression()), ctx.ID().getText());
        code += quadQueue.genIRCode();
        return code;

    }

    @Override
    public String visitFuncCallStm(calParser.FuncCallStmContext ctx) {
        String code = "";
        if (ctx.argument_list() != null)
            code += visit(ctx.argument_list());
        code += "\tcall " + ctx.ID().getText() + ", " + (paramIdx - 1);
        return code;
    }

    @Override
    public String visitFuncCallExpr(calParser.FuncCallExprContext ctx) {

        String code = "";

        if (ctx.argument_list() != null)
            code += visit(ctx.argument_list());

        code += "call " + ctx.ID().getText() + ", " + (paramIdx - 1) + "\n";

        return code;
    }

    @Override
    public String visitBeginStm(calParser.BeginStmContext ctx) {
        return "\t" + visit(ctx.statement_block()) + "\n";
    }

    @Override
    public String visitConditionalStm(calParser.ConditionalStmContext ctx) {
        String code = "";

        int jmpTo;
        boolean isAnd = false;

        String condition = visit(ctx.condition());
        String[] boolExpressions;

        if (condition.contains("&")) {
            isAnd = true;
            boolExpressions = condition.split("&");
        } else if (condition.contains("||"))
            boolExpressions = condition.split("||");
        else
            boolExpressions = new String[] {condition};

        jmpTo = jmpIdx + boolExpressions.length;

        if (isAnd) {
            for (String _if : boolExpressions) {
                code += "\t" + "ifz " + _if + " goto l" + jmpIdx + "\n";
                code += "\tgoto exit" + jmpTo + "\n";
                code += "l" + jmpIdx + ":\n";
                jmpIdx++;
            }
            jmpIdx = jmpTo;

            if (ctx.statement_block(0) != null)
                code += visit(ctx.statement_block(0));

            code += "\tgoto exit" + (jmpIdx + 1) + "\n";
            code += "exit" + jmpIdx + ":\n";
            jmpIdx++;

        } else {
            for (String _if : boolExpressions) {
                code += "\t" + "if " + _if + " goto l" + jmpIdx + "\n";
                code += "\tgoto exit" + (jmpIdx + 1) + "\n";
                code += "l" + jmpIdx + ":\n";
            }

            jmpIdx++;
            if (ctx.statement_block(0) != null)
                code += visit(ctx.statement_block(0));

            code += "\tgoto exit" + jmpIdx + "\n";
        }

        if (ctx.statement_block(1) != null)
            code += visit(ctx.statement_block(1));

        code += "exit" + jmpIdx + ":\n";

        return code;
    }

    @Override
    public String visitWhileStm(calParser.WhileStmContext ctx) {
        String code = "l" + jmpIdx + ":\n";
        if (ctx.condition() != null)
            code += "\t" + "ifz " + visit(ctx.condition()) + " goto exit" + jmpIdx + "\n";
        if (ctx.statement_block() != null)
            code += visit(ctx.statement_block());
        code += "\tgoto l" + jmpIdx + "\n";
        code += "exit" + jmpIdx + ":\n";
        jmpIdx++;
        return code;
    }

    @Override
    public String visitSkipStm(calParser.SkipStmContext ctx) {
        return "";
    }

    @Override
    public String visitFragBinArithStm(calParser.FragBinArithStmContext ctx) {
        int tmpN = quadQueue.getTmpCounter();
        quadQueue.add(visit(ctx.binary_arith_op()), visit(ctx.expression(0)), visit(ctx.expression(1)), "t" + tmpN);
        return "t" + tmpN;
    }

    @Override
    public String visitParenExpreStm(calParser.ParenExpreStmContext ctx) {
        return "";
    }

    @Override
    public String visitFragRef(calParser.FragRefContext ctx) {
        return visit(ctx.fragm());
    }

    @Override
    public String visitBoolNegStm(calParser.BoolNegStmContext ctx) {
        return ctx.NOT() + " " + visit(ctx.condition());
    }

    @Override
    public String visitParenConditionalStm(calParser.ParenConditionalStmContext ctx) {
        return visit(ctx.condition());
    }

    @Override
    public String visitBoolEvalStm(calParser.BoolEvalStmContext ctx) {
        String op;
        if (ctx.AND() != null)
            op = "&&";
        else
            op = "||";

        return visit(ctx.condition(0)) + op + visit(ctx.condition(1));
    }

    @Override
    public String visitBoolArithStm(calParser.BoolArithStmContext ctx) {
        return visit(ctx.expression(0)) + " " + visit(ctx.comp_operators()) + " " + visit(ctx.expression(1));
    }

    @Override
    public String visitIdFrag(calParser.IdFragContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitFragUnaArithStm(calParser.FragUnaArithStmContext ctx) {
        int tmpN = quadQueue.getTmpCounter();
        quadQueue.add(visit(ctx.binary_arith_op()), visit(ctx.fragm()), "t" + tmpN);
        return "t" + tmpN;
    }

    @Override
    public String visitNegationStm(calParser.NegationStmContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitIntStm(calParser.IntStmContext ctx) {
        String num = ctx.NUMBER().getText();

        if (Integer.parseInt(num) < 0)
            return "0 - " + num.substring(1);

        return num;
    }

    @Override
    public String visitTrueStm(calParser.TrueStmContext ctx) {
        return ctx.True().getText();
    }

    @Override
    public String visitFalseStm(calParser.FalseStmContext ctx) {
        return ctx.False().getText();
    }

    @Override
    public String visitNonEmptyArgListRef(calParser.NonEmptyArgListRefContext ctx) {
        String code = "";
        if (ctx.nemp_argument_list() != null)
            code += visit(ctx.nemp_argument_list());

        return code;
    }

    @Override
    public String visitIdArgRef(calParser.IdArgRefContext ctx) {
        String code = "";
        if (ctx.ID() != null)
            code += "\tparam " + ctx.ID().getText() + "\n";
        return code;
    }

    @Override
    public String visitMultIdArgRef(calParser.MultIdArgRefContext ctx) {
        String code = "";
        if (ctx.ID() != null)
            code += "\tparam " + ctx.ID().getText() + "\n";
        if (ctx.nemp_argument_list() != null)
            code += visit(ctx.nemp_argument_list());
        return code;
    }

    @Override
    public String visitAdditionStm(calParser.AdditionStmContext ctx) {
        return "+";
    }

    @Override
    public String visitSubtractionStm(calParser.SubtractionStmContext ctx) {
        return "-";
    }

    @Override
    public String visitLogOr(calParser.LogOrContext ctx) {
        return ctx.OR().getText();
    }

    @Override
    public String visitLogEq(calParser.LogEqContext ctx) {
        return ctx.EQUAL().getText();
    }

    @Override
    public String visitLogNEq(calParser.LogNEqContext ctx) {
        return ctx.NOTEQUAL().getText();
    }

    @Override
    public String visitLogLT(calParser.LogLTContext ctx) {
        return ctx.LT().getText();
    }

    @Override
    public String visitLogLTE(calParser.LogLTEContext ctx) {
        return ctx.LTE().getText();
    }

    @Override
    public String visitLogGT(calParser.LogGTContext ctx) {
        return ctx.GT().getText();
    }

    @Override
    public String visitLogGTE(calParser.LogGTEContext ctx) {
        return ctx.GTE().getText();
    }
}
