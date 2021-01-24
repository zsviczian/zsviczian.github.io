function thankyou() {
	e = document.getElementsByClassName("MyFooter");	
	let s = '<table><tbody><tr>';
	s   += '<td><p style="margin-right: 24px;">Like this blog? <br/>Show your support.</p></td><td>';
	s   += '<sc'+'ript type="text/javascript">kofiwidget2.init("Buy me a coffee", "#eb6363", "D1D5302HF");kofiwidget2.draw();</';
	s   += 'script></td></tr></tbody></table>';
	for (let i=0;i<e.length;i++) 
		e[i].innerHTML = s;
}


