[
	{name: "bla1", start: 1},
	{name: "bla2", start: 2},
	{name: "bla3", start: 5},
	{name: "bla4", start: 7}
].reduce((sum, memo) => {
	console.log("sum", sum, "memo", memo);
	memo.size = memo.start - sum;
	return memo.start;
}, 0);