package cal;

import java.util.Arrays;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.cal.antlr.calBaseVisitor;
import org.cal.antlr.calParser;

public class EvalVisitor extends calBaseVisitor<String> {
    private final SymbolTable st;

    public EvalVisitor() {
        st = new SymbolTable();
    }

    @Override
    public String visitProgStm(calParser.ProgStmContext ctx) {
        st.enterScope();
        if (ctx.decl_list() != null)
            visit(ctx.decl_list());
        if (ctx.function_list() != null)
            visit(ctx.function_list());
        if (ctx.main() != null)
            visit(ctx.main());
        st.exitScope();
        return "";
    }

    @Override
    public String visitDeclListStm(calParser.DeclListStmContext ctx) {
        if (ctx.decl() != null)
            visit(ctx.decl());
        if (ctx.decl_list() != null)
            visit(ctx.decl_list());
        return "";
    }

    @Override
    public String visitVarDeclRef(calParser.VarDeclRefContext ctx) {
        return visit(ctx.var_decl());
    }

    @Override
    public String visitConstDeclRef(calParser.ConstDeclRefContext ctx) {
        return visit(ctx.const_decl());
    }



    @Override
    public String visitVarDeclStm(calParser.VarDeclStmContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();

        st.put(id, type);
        return id;
    }

    @Override
    public String visitConstDeclStm(calParser.ConstDeclStmContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        st.put(id, type);
        return ctx.ID().getText();
    }

    @Override
    public String visitFuncListStm(calParser.FuncListStmContext ctx) {
        if (ctx.function() != null)
            visit(ctx.function());
        if (ctx.function_list() != null)
            visit(ctx.function_list());
        return "";
    }

    @Override
    public String visitFuncDeclStm(calParser.FuncDeclStmContext ctx) {

        String id = ctx.ID().getText();
        String type = ctx.type().getText();

        st.put(id, type);
        st.enterScope();

        type = type + "_" + visit(ctx.parameter_list());

        st.set(id, type);

        if (ctx.decl_list() != null)
            visit(ctx.decl_list());
        if (ctx.statement_block() != null)
            visit(ctx.statement_block());

        st.exitScope();

        return id;
    }

    @Override
    public String visitNonEmptyParamRef(calParser.NonEmptyParamRefContext ctx) {
        if (ctx.nemp_paramerter_list() != null)
            return "" + visit(ctx.nemp_paramerter_list());
        return "";
    }

    @Override
    public String visitSingleParamStm(calParser.SingleParamStmContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();

        st.put(id, type);
        return "_" + type;
    }

    @Override
    public String visitMultipleParamStm(calParser.MultipleParamStmContext ctx) {
        String type = "";
        if (ctx.ID() != null && ctx.type() != null) {
            st.put(ctx.ID().getText(), ctx.type().getText());
            return ctx.type().getText() + visit(ctx.nemp_paramerter_list());
        }
        if (ctx.nemp_paramerter_list() != null)
            return type + visit(ctx.nemp_paramerter_list());
        return type;
    }

    @Override
    public String visitMultIdArgRef(calParser.MultIdArgRefContext ctx) {
        String type = "";
        if (ctx.ID() != null)
            type += st.get(ctx.ID().getText()) + "_";
        if (ctx.nemp_argument_list() != null)
            return type + visit(ctx.nemp_argument_list());
        return type;
    }

    @Override
    public String visitIdArgRef(calParser.IdArgRefContext ctx) {
        if (ctx.ID() != null)
            if (!st.contains(ctx.ID().getText())) {
                System.out.println("ERROR @ " + ctx.start.getLine());
                System.out.println("Variable \"" + ctx.ID() + "\" has not been instantiated.");
            }
        return st.get(ctx.ID().getText());
    }

    @Override
    public String visitMainStm(calParser.MainStmContext ctx) {
        st.enterScope();

        if (ctx.decl_list() != null)
            visit(ctx.decl_list());
        if (ctx.statement_block() != null)
            visit(ctx.statement_block());
        st.exitScope();
        return "";
    }

    @Override
    public String visitStmBlockRef(calParser.StmBlockRefContext ctx) {
        if (ctx.statement() != null)
            visit(ctx.statement());
        if (ctx.statement_block() != null)
            visitChildren(ctx.statement_block());
        return "";
    }

    @Override
    public String visitAssignStm(calParser.AssignStmContext ctx) {
        String id = ctx.ID().getText();
        String exp_type = visit(ctx.expression());

        boolean instantiated;
        instantiated = st.contains(id);

        if (!instantiated) {
            System.out.println("Error @ Line: " + ctx.start.getLine());
            System.out.println("Variable: \"" + id + "\" has not been instantiated.");
            System.exit(0);
        }

        if (exp_type != null && exp_type.contains("_")) {
            String returnType = exp_type.split("_")[0];
            String type = st.get(id);
            if (!type.equals(returnType)) {
                System.out.println("Error @ Line: " + ctx.start.getLine());
                System.out.println("The return value assigned to \"" + id + "\" does not match the defined type \"" + type + "\".");
                System.exit(0);
            }
        }

        return id;
    }

    @Override
    public String visitFragBinArithStm(calParser.FragBinArithStmContext ctx) {
        boolean isInt = true;

        if (ctx.expression(0) != null)
            isInt = isInt && visit(ctx.expression(0)).equals("integer");

        if (ctx.expression(1) != null)
            isInt = isInt && visit(ctx.expression(1)).equals("integer");

        if (!isInt) {
            System.out.println("Error @ Line " + ctx.start.getLine());
            System.out.println("You are trying to preform an arithmetic operation using one or more non-integer values.");
            System.exit(0);
        }
        return "integer";
    }

    @Override
    public String visitBeginStm(calParser.BeginStmContext ctx) {
        st.put("BEGIN", "NULL");
        visit(ctx.statement_block());
        st.put("BEGIN", "NULL");
        return "";
    }

    @Override
    public String visitConditionalStm(calParser.ConditionalStmContext ctx) {
        if (ctx.condition() != null)
            visit(ctx.condition());
        if (ctx.statement_block() != null)
            visit(ctx.statement_block(0));
        if (ctx.statement_block() != null)
            visit(ctx.statement_block(1));
        return "";
    }

    @Override
    public String visitWhileStm(calParser.WhileStmContext ctx) {
        if (ctx.condition() != null)
            visit(ctx.condition());
        if (ctx.statement_block() != null)
            visit(ctx.statement_block());
        return "";
    }

    @Override
    public String visitFuncCallStm(calParser.FuncCallStmContext ctx) {
        return funcHandler(ctx.ID(), ctx.argument_list(), ctx);
    }

    @Override
    public String visitFuncCallExpr(calParser.FuncCallExprContext ctx) {
        return funcHandler(ctx.ID(), ctx.argument_list(), ctx);
    }

    private String funcHandler(TerminalNode id2, calParser.Argument_listContext argument_listContext, ParserRuleContext ctx) {
        String id = id2.getText();

        if (!st.contains(id)) {
            System.out.println("Error @ Line " + ctx.start.getLine());
            System.out.println("Function: \"" + id + "\" has not been defined.");
            System.exit(0);
        }

        String compositeType = st.get(id);
        String[] argTypesArray = compositeType.split("_");

        if (argTypesArray.length == 1)
            return st.get(id);

        String argTypes = String.join("_", Arrays.copyOfRange(argTypesArray, 1 , argTypesArray.length));

        if (!argTypes.equals(visit(argument_listContext))) {
            System.out.println("Error @ Line " + ctx.start.getLine());
            System.out.print("Function: \"" + id + "\" requires " + (argTypesArray.length - 1) + " arguments of types ");
            for (String arg : Arrays.copyOfRange(argTypesArray, 1 , argTypesArray.length))
                System.out.print(arg + ", ");
            System.out.println();
            System.exit(0);
        }

        return st.get(id);
    }

    @Override
    public String visitIdFrag(calParser.IdFragContext ctx) {
        String id = ctx.ID().getText();
        String type = st.get(id);

        if (type == null) {
            System.out.println("Error @ Line " + ctx.start.getLine());
            System.out.println("Variable \"" + id + "\" has not been declared");
            System.exit(0);
        }
        return type;
    }

    @Override
    public String visitIntStm(calParser.IntStmContext ctx) {
        return "integer";
    }

    @Override
    public String visitNegationStm(calParser.NegationStmContext ctx) {
        return "integer";
    }

    @Override
    public String visitType(calParser.TypeContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitTrueStm(calParser.TrueStmContext ctx) {
        return ctx.True().getText();
    }

    @Override
    public String visitFalseStm(calParser.FalseStmContext ctx) {
        return ctx.False().getText();
    }
}