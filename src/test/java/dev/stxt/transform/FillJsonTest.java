package dev.stxt.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.exceptions.TransformException;
import dev.stxt.runtime.TreeJson;

/**
 * The fill of Slots of STXT-TRANSFORM-SPEC §4 (Genesis), rule by rule. The filled document is
 * compared by its canonical tree with the document the rule says it gives; an error, by its code
 * (provisional while the specification is in Genesis) and a fragment of its message.
 */
public class FillJsonTest {

	private static String doc(String... lines) {
		return String.join("\n", lines);
	}

	// JSON with single quotes, to keep the data readable in Java strings
	private static String json(String data) {
		return data.replace('\'', '"');
	}

	private static String canonical(String document) {
		return TreeJson.toCanonicalJson(new Parser().parse(document));
	}

	private static String fill(String document, String data) {
		return TreeJson.toCanonicalJson(new FillJson().fill(new Parser().parse(document), json(data)));
	}

	private static void assertFills(String expected, String document, String data) {
		assertEquals(canonical(expected), fill(document, data));
	}

	private static TransformException assertFails(String code, String message, String document, String data) {
		TransformException e = assertThrows(TransformException.class, () -> fill(document, data));
		assertEquals(code, e.getCode(), e.toString());
		assertTrue(e.getMessage().contains(message), e.toString());
		return e;
	}

	// ----------------------------------------------------------------
	// The example of §4
	// ----------------------------------------------------------------

	@Test
	void fillsTheExampleOfTheSpecification() {
		assertEquals(canonical(DemoFillJson.EXPECTED),
				TreeJson.toCanonicalJson(new FillJson().fill(new Parser().parse(DemoFillJson.DOCUMENT), DemoFillJson.DATA)));
	}

	// ----------------------------------------------------------------
	// What is copied as it is
	// ----------------------------------------------------------------

	@Test
	void copiesAValueWithoutSlotsAsItIs() {
		assertFills(doc("Pedido: 1", "\tMoneda: EUR"), doc("Pedido: 1", "\tMoneda: EUR"), "{}");
	}

	@Test
	void doesNotCopyComments() {
		assertFills(doc("Pedido: 1"), doc("# A comment", "Pedido: <n>", "\t# Another"), "{'n': 1}");
	}

	@Test
	void keepsTheNamespaceWhereTheDocumentDeclaresIt() {
		assertFills(doc("Pedido (com.example.pedidos): 7", "\tFecha: hoy"),
				doc("Pedido (com.example.pedidos): <n>", "\tFecha: <f>"), "{'n': 7, 'f': 'hoy'}");
	}

	@Test
	void needsNeitherANamespaceNorADefinitionAndDoesNotValidate() {
		// Cantidad is not a number in the document with Slots, and nothing defines com.example.x
		assertFills(doc("Pedido (com.example.x):", "\tCantidad: tres"),
				doc("Pedido (com.example.x):", "\tCantidad: <cantidad>"), "{'cantidad': 'tres'}");
	}

	@Test
	void fillsEveryRootNode() {
		assertFills(doc("A: 1", "B: 2"), doc("A: <a>", "B: <b>"), "{'a': 1, 'b': 2}");
	}

	// ----------------------------------------------------------------
	// <var> and ${var}
	// ----------------------------------------------------------------

	@Test
	void putsTheTextOfStringsNumbersAndBooleans() {
		// Decimals keep their trailing zeros, and nothing is written in exponent form
		assertFills(doc("V:", "\tS: texto", "\tN: 40", "\tD: 1.10", "\tZ: 100.0", "\tG: 12345678901234567890", "\tE: 1000", "\tB: true"),
				doc("V:", "\tS: <s>", "\tN: <n>", "\tD: <d>", "\tZ: <z>", "\tG: <g>", "\tE: <e>", "\tB: <b>"),
				"{'s': 'texto', 'n': 40, 'd': 1.10, 'z': 100.0, 'g': 12345678901234567890, 'e': 1E+3, 'b': true}");
	}

	@Test
	void aVarIsTheValueOfTheNodeAndCanHaveChildren() {
		assertFills(doc("Pedido: 7", "\tFecha: hoy"), doc("Pedido: <n>", "\tFecha: <f>"), "{'n': 7, 'f': 'hoy'}");
	}

	@Test
	void aVarEndsTheValue() {
		String data = "{'a': 1, 'b': 2, 'producto': 'x', 'lineas': [{'producto': 'y'}]}";
		assertFills(doc("V: 1"), doc("V: <a>  "), data);
		assertFails("SLOT_MISPLACED", "Nothing can follow <a>, which is the value of the node", doc("V: <a> <b>"), data);
		assertFails("SLOT_MISPLACED", "Nothing can follow <a>", doc("V: <a> (${b})"), data);
		assertFails("SLOT_MISPLACED", "Nothing can follow <producto>", doc("Línea: <producto>[lineas]"), data);
		assertFails("SLOT_MISPLACED", "Nothing can follow <producto>", doc("Línea: [lineas]<producto> extra"), data);
	}

	@Test
	void aVarAndATextWithOnlyAVarGiveTheSame() {
		assertFills(doc("A: hoy", "B: hoy"), doc("A: <f>", "B: ${f}"), "{'f': 'hoy'}");
	}

	@Test
	void putsSeveralVarsInsideATextAndCopiesTheTextAround() {
		assertFills(doc("Referencia: Pedido 2026-0142/Correos."),
				doc("Referencia: Pedido ${numero}/${transportista}."), "{'numero': '2026-0142', 'transportista': 'Correos'}");
	}

	@Test
	void putsVarsInAnyLineOfABlock() {
		assertFills(doc("Instrucciones >>", "\tPedido 7, del lunes.", "\tEntregar por la mañana.", "\tFirma: Ana"),
				doc("Instrucciones >>", "\tPedido ${n}, del ${dia}.", "\tEntregar por la mañana.", "\tFirma: ${firma}"),
				"{'n': 7, 'dia': 'lunes', 'firma': 'Ana'}");
	}

	// ----------------------------------------------------------------
	// Names of Slots, and what is text
	// ----------------------------------------------------------------

	@Test
	void aSlotNameHasAsciiLettersDigitsHyphensAndUnderscores() {
		assertFills(doc("V: a b c d", "W: e"), doc("V: ${x1} ${datos-cliente} ${id_2} ${X}", "W: [mis-envios_2]"),
				"{'x1': 'a', 'datos-cliente': 'b', 'id_2': 'c', 'X': 'd', 'mis-envios_2': ['e']}");
	}

	@Test
	void whatDoesNotHaveTheFormOfASlotIsText() {
		assertFills(doc("V: </p> <a href=\"x\"> <> <número> [] {} ${} ${a b} ${número} $x $"),
				doc("V: </p> <a href=\"x\"> <> <número> [] {} ${} ${a b} ${número} $x $"), "{}");
	}

	@Test
	void inTheTextAngleSquareAndCurlyBracketsAreText() {
		assertFills(doc("Etiqueta: Ver [nota] y {nota} en <b>", "Html >>", "\t<p>Hola, Ana</p>", "\t<br>"),
				doc("Etiqueta: Ver [nota] y {nota} en <b>", "Html >>", "\t<p>Hola, ${nombre}</p>", "\t<br>"), "{'nombre': 'Ana'}");
	}

	@Test
	void whatHasTheFormOfAVarIsAVar() {
		assertFails("SLOT_NO_DATA", "No data for ${HOME}", doc("Script >>", "\techo \"${HOME}\""), "{}");
	}

	@Test
	void twoDollarsBeforeABraceAreALiteralDollarAndBrace() {
		assertFills(doc("Script >>", "\techo \"${HOME}\" 7"), doc("Script >>", "\techo \"$${HOME}\" ${n}"), "{'n': 7}");
		assertFills(doc("V: ${n} es 7, ${a b}"), doc("V: $${n} es ${n}, $${a b}"), "{'n': 7}");
		// Elsewhere, $$ is copied as it is
		assertFills(doc("Fórmula >>", "\t$$x^2$$ y 7 $$"), doc("Fórmula >>", "\t$$x^2$$ y ${n} $$"), "{'n': 7}");
	}

	@Test
	void aSlotNameIsLookedUpAsItIsWithCase() {
		assertFails("SLOT_NO_DATA", "No data for <Fecha>", doc("Fecha: <Fecha>"), "{'fecha': 'hoy'}");
		assertFails("SLOT_NO_DATA", "No data for ${Fecha}", doc("Fecha: ${Fecha}"), "{'fecha': 'hoy'}");
	}

	// ----------------------------------------------------------------
	// Scope
	// ----------------------------------------------------------------

	@Test
	void aSlotLooksUpItsDataInTheCurrentElementAndThenUpwards() {
		assertFills(doc("Envío: Correos", "\tReferencia: 7/Correos", "Envío: DHL", "\tReferencia: 7/DHL"),
				doc("Envío: [envios]<transportista>", "\tReferencia: ${numero}/${transportista}"),
				"{'numero': 7, 'envios': [{'transportista': 'Correos'}, {'transportista': 'DHL'}]}");
	}

	@Test
	void theNearestDataHidesTheOneAbove() {
		assertFills(doc("Cliente:", "\tNombre: interior", "Nombre: raíz"),
				doc("Cliente: {cliente}", "\tNombre: <nombre>", "Nombre: <nombre>"),
				"{'nombre': 'raíz', 'cliente': {'nombre': 'interior'}}");
	}

	@Test
	void anObjectAppliesToTheNodeAndToItsChildren() {
		assertFills(doc("Cliente: Atenea", "\tEmail: a@example.com"),
				doc("Cliente: {cliente}<nombre>", "\tEmail: <email>"),
				"{'cliente': {'nombre': 'Atenea', 'email': 'a@example.com'}}");
	}

	@Test
	void aNullCountsAsAbsentAndTheLookupGoesOnUpwards() {
		assertFills(doc("Cliente:", "\tNombre: raíz"), doc("Cliente: {cliente}", "\tNombre: <nombre>"),
				"{'nombre': 'raíz', 'cliente': {'nombre': null}}");
		assertFails("SLOT_NO_DATA", "No data for <nombre>", doc("Nombre: <nombre>"), "{'nombre': null}");
	}

	// ----------------------------------------------------------------
	// Required and optional
	// ----------------------------------------------------------------

	@Test
	void everySlotIsRequired() {
		assertFails("SLOT_NO_DATA", "No data for <cantidad>", doc("Cantidad: <cantidad>"), "{}");
		assertFails("SLOT_NO_DATA", "No data for ${cantidad}", doc("Cantidad: ${cantidad} uds."), "{}");
		assertFails("SLOT_NO_DATA", "No data for {cliente}", doc("Cliente: {cliente}"), "{}");
		assertFails("SLOT_NO_DATA", "No data for [lineas]", doc("Línea: [lineas]"), "{}");
	}

	@Test
	void anOptionalVarWithNoDataRemovesTheNodeWithItsChildren() {
		assertFills(doc("Cliente:"), doc("Cliente:", "\tTeléfono: ?<telefono>", "\t\tTipo: móvil"), "{}");
		assertFills(doc("Cliente:", "\tTeléfono: 600"), doc("Cliente:", "\tTeléfono: ?<telefono>"), "{'telefono': 600}");
	}

	@Test
	void anOptionalVarOfTheTextWithNoDataPutsNothingAndTheNodeStays() {
		assertFills(doc("Cliente:", "\tTeléfono:"), doc("Cliente:", "\tTeléfono: ${?telefono}"), "{}");
	}

	@Test
	void anOptionalVarInsideATextOnlyRemovesItsPiece() {
		assertFills(doc("Referencia: 2026-0142", "Notas >>", "\tRevisión: ."),
				doc("Referencia: ${numero}${?revision}", "Notas >>", "\tRevisión: ${?revision}."), "{'numero': '2026-0142'}");
	}

	@Test
	void anOptionalListOrObjectWithNoDataRemovesTheNodeWithItsChildren() {
		assertFills(doc("Pedido: 1"),
				doc("Pedido: 1", "\tLínea: ?[lineas]", "\t\tCantidad: <cantidad>", "\tCliente: ?{cliente}", "\t\tNombre: <nombre>"),
				"{}");
	}

	@Test
	void anOptionalObjectWithANullRemovesTheNode() {
		assertFills(doc("Pedido: 1"), doc("Pedido: 1", "\tCliente: ?{cliente}", "\t\tNombre: <nombre>"), "{'cliente': null}");
	}

	@Test
	void anEmptyArrayGivesNoNodeAndIsNotAnError() {
		assertFills(doc("Pedido: 1"), doc("Pedido: 1", "\tLínea: [lineas]<producto>"), "{'lineas': []}");
	}

	// ----------------------------------------------------------------
	// Data that does not fit its Slot
	// ----------------------------------------------------------------

	@Test
	void dataThatDoesNotFitItsSlotIsAnError() {
		assertFails("SLOT_DATA_MISMATCH", "<cliente> expects a string, a number or a boolean", doc("Cliente: <cliente>"), "{'cliente': {}}");
		assertFails("SLOT_DATA_MISMATCH", "<lineas> expects a string, a number or a boolean", doc("Línea: <lineas>"), "{'lineas': [1]}");
		assertFails("SLOT_DATA_MISMATCH", "${cliente} expects a string, a number or a boolean", doc("Cliente: ${cliente}"), "{'cliente': {}}");
		assertFails("SLOT_DATA_MISMATCH", "[lineas] expects an array", doc("Línea: [lineas]"), "{'lineas': 'x'}");
		assertFails("SLOT_DATA_MISMATCH", "[lineas] expects an array", doc("Línea: [lineas]"), "{'lineas': {}}");
		assertFails("SLOT_DATA_MISMATCH", "{cliente} expects an object", doc("Cliente: {cliente}"), "{'cliente': 'x'}");
		assertFails("SLOT_DATA_MISMATCH", "{cliente} expects an object", doc("Cliente: {cliente}"), "{'cliente': []}");
	}

	@Test
	void theDataIsAJsonText() {
		assertFails("JSON_NOT_VALID", "Unexpected end of the JSON text", doc("V: <v>"), "{'v': ");
	}

	// ----------------------------------------------------------------
	// Lists
	// ----------------------------------------------------------------

	@Test
	void aListRepeatsTheNodeWithItsChildrenOncePerElement() {
		assertFills(doc("Línea: Cuaderno", "\tCantidad: 40", "Línea: Lápiz", "\tCantidad: 200"),
				doc("Línea: [lineas]<producto>", "\tCantidad: <cantidad>"),
				"{'lineas': [{'producto': 'Cuaderno', 'cantidad': 40}, {'producto': 'Lápiz', 'cantidad': 200}]}");
	}

	@Test
	void aListAtTheRootGivesSeveralRootNodes() {
		assertFills(doc("Item: a", "Item: b"), doc("Item: [items]"), "{'items': ['a', 'b']}");
	}

	@Test
	void anElementThatIsAValueIsTheValueOfTheNode() {
		assertFills(doc("Bulto: C-01", "Bulto: 2", "Bulto: true"), doc("Bulto: [bultos]"), "{'bultos': ['C-01', 2, true]}");
	}

	@Test
	void anElementThatIsAValueIsTheLineOfABlock() {
		assertFills(doc("Nota >>", "\tuno", "Nota >>", "\tdos", "\tlíneas"), doc("Nota >>", "\t[notas]"),
				"{'notas': ['uno', 'dos\\nlíneas']}");
	}

	@Test
	void aNodeThatTakesAValueFromTheListHasNothingElse() {
		String data = "{'bultos': ['C-01', 'C-02'], 'peso': 3}";
		assertFails("SLOT_MISPLACED", "has nothing else", doc("Bulto: [bultos] (caja)"), data);
		assertFails("SLOT_MISPLACED", "has nothing else", doc("Bulto: [bultos] ${peso}"), data);
		assertFails("SLOT_MISPLACED", "has nothing else", doc("Bulto: [bultos]", "\tPeso: <peso>"), data);
		assertFails("SLOT_MISPLACED", "has nothing else", doc("Bulto >>", "\t[bultos]", "\tPeso: ${peso}"), data);
	}

	@Test
	void noNavigationCanFollowAListOfValues() {
		String data = "{'bultos': ['C-01'], 'medidas': {}, 'peso': 3}";
		assertFails("SLOT_MISPLACED", "nothing can follow it: {medidas}", doc("Bulto: [bultos]{medidas}"), data);
		assertFails("SLOT_MISPLACED", "nothing can follow it: <peso>", doc("Bulto: [bultos]<peso>"), data);
	}

	@Test
	void anArrayThatMixesValuesAndObjectsIsAnError() {
		assertFails("SLOT_DATA_MISMATCH", "[bultos] mixes values and objects", doc("Bulto: [bultos]"), "{'bultos': ['C-01', {'id': 'C-02'}]}");
	}

	@Test
	void anArrayInsideAnArrayIsAnError() {
		assertFails("SLOT_DATA_MISMATCH", "[bultos] has an array inside the array", doc("Bulto: [bultos]"), "{'bultos': [['C-01']]}");
	}

	@Test
	void aNullElementIsAsIfItWereNotThere() {
		assertFills(doc("Bulto: C-01", "Bulto: C-02"), doc("Bulto: [bultos]"), "{'bultos': ['C-01', null, 'C-02']}");
		assertFills(doc("Línea: a"), doc("Línea: [lineas]<p>"), "{'lineas': [null, {'p': 'a'}]}");
	}

	// ----------------------------------------------------------------
	// The two parts of the value: navigation, then text
	// ----------------------------------------------------------------

	@Test
	void navigationIsAppliedFromLeftToRight() {
		String data = "{'cliente': {'nombre': 'Atenea', 'direcciones': [{'calle': 'Mayor'}, {'calle': 'Sol'}]}}";
		assertFills(doc("Dirección: Atenea, Mayor", "Dirección: Atenea, Sol"),
				doc("Dirección: {cliente}[direcciones]${nombre}, ${calle}"), data);
		assertFills(doc("Dirección: Mayor", "Dirección: Sol"), doc("Dirección: {cliente}[direcciones]<calle>"), data);
	}

	@Test
	void eachListRepeatsOverWhatThePreviousOneLeaves() {
		assertFills(doc("Bulto: a1", "Bulto: a2", "Bulto: b1"), doc("Bulto: [envios]{carga}[bultos]"),
				"{'envios': [{'carga': {'bultos': ['a1', 'a2']}}, {'carga': {'bultos': ['b1']}}]}");
	}

	@Test
	void blanksAreAllowedBetweenTheNavigationAndBeforeTheText() {
		String data = "{'cliente': {'direcciones': [{'calle': 'Mayor'}]}}";
		assertFills(doc("Dirección: Mayor"), doc("Dirección: {cliente} [direcciones]   <calle>"), data);
		assertFills(doc("Dirección: Mayor"), doc("Dirección: {cliente}\t[direcciones]${calle}"), data);
	}

	@Test
	void theTextCannotRepeatNorEnterNorBeTheValue() {
		// What would be navigation is text once the text has started
		assertFills(doc("Línea: x [lineas] {nota} <producto>"), doc("Línea: ${producto} [lineas] {nota} <producto>"),
				"{'producto': 'x'}");
	}

	// ----------------------------------------------------------------
	// Blocks
	// ----------------------------------------------------------------

	@Test
	void theFirstLineOfABlockWithOnlyNavigationIsNotCopied() {
		assertFills(doc("Etiqueta >>", "\tTransportista: Correos", "Etiqueta >>", "\tTransportista: DHL"),
				doc("Etiqueta >>", "\t[envios]", "\tTransportista: ${transportista}"),
				"{'envios': [{'transportista': 'Correos'}, {'transportista': 'DHL'}]}");
	}

	@Test
	void theFirstLineOfABlockWithMoreIsText() {
		assertFills(doc("Etiqueta >>", "\tPedido 7"), doc("Etiqueta >>", "\tPedido ${n}"), "{'n': 7}");
		assertFills(doc("Etiqueta >>", "\tPedido", "\t[envios]"), doc("Etiqueta >>", "\tPedido", "\t[envios]"), "{}");
		assertFills(doc("Etiqueta >>", "\t<b>[nota]</b>"), doc("Etiqueta >>", "\t<b>[nota]</b>"), "{}");
	}

	@Test
	void aFirstLineOfABlockThatStartsWithNavigationHasNothingElse() {
		String data = "{'envios': [{'t': 'x'}], 'cliente': {}}";
		String message = "The first line of a block that starts with navigation has nothing else: ";
		assertFills(doc("Etiqueta >>", "\tx"), doc("Etiqueta >>", "\t  {cliente} [envios]  ", "\t${t}"),
				"{'cliente': {'envios': [{'t': 'x'}]}}");
		assertFails("SLOT_MISPLACED", message + "Pedido", doc("Etiqueta >>", "\t[envios] Pedido", "\tTexto"), data);
		assertFails("SLOT_MISPLACED", message + "<t>", doc("Etiqueta >>", "\t[envios]<t>"), data);
		assertFails("SLOT_MISPLACED", message + "${t}", doc("Etiqueta >>", "\t{cliente} ${t}"), data);
		assertFails("SLOT_MISPLACED", message + "x", doc("Etiqueta >>", "\t[parece_navegacion]{pero-no-lo-es} x"), data);
		// A Markdown link at the start of a block has the form of a Slot
		assertFails("SLOT_MISPLACED", message + "(index)", doc("Menú >>", "\t[Inicio](index)"), data);
	}

	@Test
	void aVarIsNeverNavigationInABlock() {
		assertFills(doc("Html >>", "\t<html>", "\t<p>x</p>"), doc("Html >>", "\t<html>", "\t<p>${t}</p>"), "{'t': 'x', 'html': 'no'}");
		assertFills(doc("Html >>", "\t<b>[nota]</b> ${t}"), doc("Html >>", "\t<b>[nota]</b> ${t}"), "{'t': '${t}'}");
	}

	@Test
	void anEmptyOrBlankFirstLineOfABlockIsText() {
		assertFills(doc("Notas >>", "\t", "\tHola Ana"), doc("Notas >>", "\t", "\tHola ${nombre}"), "{'nombre': 'Ana'}");
		assertFills(doc("Notas >>", "\t", "\tHola Ana"), doc("Notas >>", "\t    ", "\tHola ${nombre}"), "{'nombre': 'Ana'}");
	}

	@Test
	void aBlockKeepsTheIndentationOfItsLines() {
		assertFills(doc("Código >>", "\tif x:", "\t    y = 1"), doc("Código >>", "\tif ${cond}:", "\t    y = ${v}"),
				"{'cond': 'x', 'v': 1}");
	}

	// ----------------------------------------------------------------
	// The form of the node
	// ----------------------------------------------------------------

	@Test
	void theNodeKeepsItsForm() {
		assertFills(doc("Nota: hola", "Texto >>", "\thola"), doc("Nota: <t>", "Texto >>", "\t${t}"), "{'t': 'hola'}");
	}

	@Test
	void dataWithLineBreaksIsAnErrorInlineAndSeveralLinesInABlock() {
		assertFails("SLOT_DATA_MISMATCH", "An inline value cannot have line breaks", doc("Nota: <t>"), "{'t': 'uno\\ndos'}");
		assertFails("SLOT_DATA_MISMATCH", "An inline value cannot have line breaks", doc("Nota: A ${t}"), "{'t': 'uno\\ndos'}");
		assertFills(doc("Nota >>", "\tA: uno", "\tdos."), doc("Nota >>", "\tA: ${t}."), "{'t': 'uno\\ndos'}");
	}

	@Test
	void theFilledDocumentHasTheTreeItsTextWouldGive() {
		// Inline: no blanks at either end (STXT-SPEC 5.1); inside the text, they stay
		assertFills(doc("Nota: hola"), doc("Nota: <t>"), "{'t': '  hola \\t'}");
		assertFills(doc("Nota: [ a  ]"), doc("Nota: [${t}]"), "{'t': ' a  '}");
		// Block: the leading blanks of a line and the empty lines in between are content; the
		// trailing blanks (10.2) and the final empty lines (10.3) are not
		String data = "{'t': '  uno  \\n\\ndos\\t\\n  \\n\\n'}";
		assertFills(doc("Nota >>", "\t  uno", "\t", "\tdos"), doc("Nota >>", "\t${t}"), data);
		TextNode block = (TextNode) new FillJson().fill(new Parser().parse(doc("Nota >>", "\t${t}")), json(data)).get(0);
		assertEquals(List.of("  uno", "", "dos"), block.getTextLines());
	}

	@Test
	void theTextOfABlockAroundTheDataIsNormalizedToo() {
		// An optional Slot with no data leaves a blank at the end of a line, and a last line empty
		assertFills(doc("Nota >>", "\tA: x", "\tB"), doc("Nota >>", "\tA: ${t} ${?falta}", "\tB", "\t${?falta}"), "{'t': 'x'}");
		TextNode block = (TextNode) new FillJson().fill(new Parser().parse(doc("Nota >>", "\tA: ${t} ${?falta}", "\tB", "\t${?falta}")),
				json("{'t': 'x'}")).get(0);
		assertEquals(List.of("A: x", "B"), block.getTextLines());
	}

	// ----------------------------------------------------------------
	// Errors
	// ----------------------------------------------------------------

	@Test
	void anErrorSaysTheLineAndTheNode() {
		TransformException e = assertFails("SLOT_NO_DATA", "Node 'Fecha': No data for <fecha>", doc("Pedido: 1", "\tFecha: <fecha>"), "{}");
		assertEquals(2, e.getLine());
	}
}
