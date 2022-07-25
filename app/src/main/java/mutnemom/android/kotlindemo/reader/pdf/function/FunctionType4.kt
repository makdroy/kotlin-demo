package mutnemom.android.kotlindemo.reader.pdf.function

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import java.util.*
import kotlin.math.*

class FunctionType4 : PdfFunction(TYPE_4) {

    companion object {
        /* the set of all Operations we support.
         * These operations are defined in Appendix B - Operators.
         * */
        private var operationSet: HashSet<Operation>? = null
    }


    /* the stack of operations. The stack contents should all be Comparable.  */
    private val stack = LinkedList<Any>()

    init {
        if (operationSet == null) {
            initOperations()
        }
    }

    override fun doFunction(
        inputs: FloatArray,
        inputOffset: Int,
        outputs: FloatArray,
        outputOffset: Int
    ) {
    }

    override fun parse(obj: PdfObject) {
        throw IllegalStateException("Unsupported function type 4.")
    }

    private fun initOperations() {
        if (operationSet == null) {
            operationSet = HashSet()
        }

        // Arithmetic Operators
        operationSet!!.add(object : Operation("abs") {
            override fun eval() {
                pushDouble(abs(popDouble()))
            }
        })

        operationSet!!.add(object : Operation("add") {
            override fun eval() {
                pushDouble(popDouble() + popDouble())
            }
        })

        operationSet!!.add(object : Operation("atan") {
            override fun eval() {
                val den: Double = popDouble()
                val num: Double = popDouble()
                if (den == 0.0) {
                    pushDouble(90.0)
                } else {
                    pushDouble(Math.toDegrees(atan(num / den)))
                }
            }
        })

        operationSet!!.add(object : Operation("ceiling") {
            override fun eval() {
                pushDouble(ceil(popDouble()))
            }
        })

        operationSet!!.add(object : Operation("cvi") {
            override fun eval() {
                pushDouble(popDouble().toInt().toDouble())
            }
        })

        operationSet!!.add(object : Operation("cvr") {
            override fun eval() {
            }
        })

        operationSet!!.add(object : Operation("div") {
            override fun eval() {
                val num2: Double = popDouble()
                val num1: Double = popDouble()
                pushDouble(num1 / num2)
            }
        })

        operationSet!!.add(object : Operation("exp") {
            override fun eval() {
                val exponent: Double = popDouble()
                val base: Double = popDouble()
                pushDouble(exponent.pow(base))
            }
        })

        operationSet!!.add(object : Operation("floor") {
            override fun eval() {
                pushDouble(floor(popDouble()))
            }
        })

        operationSet!!.add(object : Operation("idiv") {
            override fun eval() {
                val int2: Long = popLong()
                val int1: Long = popLong()
                pushLong(int1 / int2)
            }
        })
        
        operationSet!!.add(object : Operation("ln") {
            override fun eval() {
                pushDouble(ln(popDouble()))
            }
        })

        operationSet!!.add(object : Operation("log") {
            override fun eval() {
                pushDouble(log10(popDouble()))
            }
        })

        operationSet!!.add(object : Operation("mod") {
            override fun eval() {
                val int2: Long = popLong()
                val int1: Long = popLong()
                pushLong(int1 % int2)
            }
        })

        operationSet!!.add(object : Operation("mul") {
            override fun eval() {
                pushDouble(popDouble() * popDouble())
            }
        })

        operationSet!!.add(object : Operation("neg") {
            override fun eval() {
                pushDouble(-popDouble())
            }
        })

        operationSet!!.add(object : Operation("round") {
            override fun eval() {
                pushLong(popDouble().roundToLong())
            }
        })

        operationSet!!.add(object : Operation("sin") {
            override fun eval() {
                val radians = Math.toRadians(popDouble())
                pushDouble(Math.toDegrees(sin(radians)))
            }
        })

        operationSet!!.add(object : Operation("sqrt") {
            override fun eval() {
                pushDouble(sqrt(popDouble()))
            }
        })

        operationSet!!.add(object : Operation("sub") {
            override fun eval() {
                val num2: Double = popDouble()
                val num1: Double = popDouble()
                pushDouble(num1 - num2)
            }
        })

        operationSet!!.add(object : Operation("truncate") {
            override fun eval() {
                val num1: Double = popDouble()
                pushDouble(num1.toLong().toDouble() - num1)
            }
        })

        // Relational, boolean, and bitwise operators
        operationSet!!.add(object : Operation("and") {
            override fun eval() {
                pushLong(popLong() and popLong())
            }
        })

        operationSet!!.add(object : Operation("bitshift") {
            override fun eval() {
                val shift: Long = popLong()
                val int1: Long = popLong()
                pushLong(int1 shl shift.toInt())
            }
        })

        operationSet!!.add(object : Operation("eq") {
            override fun eval() {
                pushBoolean(popObject() == popObject())
            }
        })

        operationSet!!.add(object : Operation("false") {
            override fun eval() {
                pushBoolean(false)
            }
        })

        operationSet!!.add(object : Operation("ge") {
            override fun eval() {
                val num2: Double = popDouble()
                val num1: Double = popDouble()
                pushBoolean(num1 >= num2)
            }
        })

        operationSet!!.add(object : Operation("gt") {
            override fun eval() {
                val num2: Double = popDouble()
                val num1: Double = popDouble()
                pushBoolean(num1 > num2)
            }
        })

        operationSet!!.add(object : Operation("le") {
            override fun eval() {
                val num2: Double = popDouble()
                val num1: Double = popDouble()
                pushBoolean(num1 <= num2)
            }
        })

        operationSet!!.add(object : Operation("lt") {
            override fun eval() {
                val num2: Double = popDouble()
                val num1: Double = popDouble()
                pushBoolean(num1 < num2)
            }
        })

        operationSet!!.add(object : Operation("ne") {
            override fun eval() {
                pushBoolean(popObject() != popObject())
            }
        })

        operationSet!!.add(object : Operation("not") {
            override fun eval() {
                pushLong(popLong().inv())
            }
        })

        operationSet!!.add(object : Operation("or") {
            override fun eval() {
                pushLong(popLong() or popLong())
            }
        })

        operationSet!!.add(object : Operation("true") {
            override fun eval() {
                pushBoolean(true)
            }
        })

        operationSet!!.add(object : Operation("xor") {
            override fun eval() {
                pushLong(popLong() xor popLong())
            }
        })

        // Conditional Operators
        operationSet!!.add(object : Operation("if") {
            override fun eval() {
                if (popBoolean()) {
                    stack.addFirst(popExpression())
                } else {
                    popExpression()
                }
            }
        })

        operationSet!!.add(object : Operation("ifelse") {
            override fun eval() {
                // execute expr1 if bool is true, expr2 if false
                if (popBoolean()) {
//                        expression.push(popExpression());
                    popExpression()
                } else {
                    popExpression()
                    //                        expression.push(popExpression());
                }
            }
        })

        // Stack Operators
        operationSet!!.add(object : Operation("copy") {
            override fun eval() {
                val obj: Any = stack.removeFirst()
                stack.addFirst(obj)
                stack.addFirst(obj)
            }
        })

        operationSet!!.add(object : Operation("dup") {
            override fun eval() {
                val obj: Any = popObject()
                pushObject(obj)
                pushObject(obj)
            }
        })

        operationSet!!.add(object : Operation("exch") {
            override fun eval() {
                // <i>any1 any2</i> <b>exch</b> <i>any2 any1</i> - exchange top of stack
                val any1: Any = popObject()
                val any2: Any = popObject()
                pushObject(any2)
                pushObject(any1)
            }
        })

        operationSet!!.add(object : Operation("index") {
            override fun eval() {
                // <i>anyn ... any0 n</i> <b>index</b> <i>anyn ... any0 anyn</i>
                val obj: Any = stack.removeFirst()
                stack.addFirst(obj)
                stack.addFirst(obj)
            }
        })

        operationSet!!.add(object : Operation("pop") {
            override fun eval() {
                // discard top element
                stack.removeFirst()
            }
        })

        operationSet!!.add(object : Operation("roll") {
            override fun eval() {
                // <i>anyn-1 ... any0 n j</i> <b>roll</b> <i>any(j-1)mod n ... anyn-1 ... any</i>
                // Roll n elements up j times
                val obj: Any = stack.removeFirst()
                stack.addFirst(obj)
                stack.addFirst(obj)
            }
        })
    }

    private fun popBoolean(): Boolean = false
    private fun pushBoolean(arg: Boolean) {}

    private fun popDouble(): Double = 0.0
    private fun pushDouble(arg: Double) {}

    private fun popExpression(): Expression? = null
    private fun pushExpression(expression: Expression) {}

    private fun popLong(): Long = 0L
    private fun pushLong(arg: Long) {}

    private fun popObject(): Any {
        return stack.removeFirst()
    }

    private fun pushObject(obj: Any) {
        stack.addFirst(obj)
    }

    internal class Expression : LinkedList<Any?>() {
        override fun equals(obj: Any?): Boolean {
            return obj is Expression
        }
    }

    internal abstract class Operation(operatorName: String?) {
        val operatorName: String

        abstract fun eval()

        override fun equals(obj: Any?): Boolean {
            if (obj is Operation) {
                return obj.operatorName == operatorName
            } else if (obj is String) {
                return operatorName == obj
            }
            return false
        }

        init {
            if (operatorName == null) {
                throw RuntimeException("Cannot have a null operator name")
            }
            this.operatorName = operatorName
        }
    }

}
