// Copyright ©2020 MarkLogic Corporation.
const LiteGraphHelper = {
  data() {},

  methods: {

   graphStatistics: function(liteGraph) {
      console.log("Information about graph: ")
      console.log("========================")
      var g = liteGraph
      console.log("Graph contains " + g.nodes.length + " nodes")
      var blockMap = new Map();
      for (var n = 0; n < g.nodes.length; n++) {
      var val = blockMap.get( g.nodes[n].type )
      if ( val != undefined ) { blockMap.set(g.nodes[n].type,++val)
      } else {
        blockMap.set(g.nodes[n].type,1)
      }
        }
    // report results
        for (let nodeType of blockMap.keys()) {
          console.log(nodeType + " x" + blockMap.get(nodeType))
        }
       console.log("========================")
    },

  listTheGraphBlocks: function(pipesModels) {
    console.log('Pipes Blocks contains: ' + pipesModels.length + " blocks:")
    for (var i = 0; i < pipesModels.length; i++) { 
      console.log( pipesModels[i].source + "/" + pipesModels[i].label )
      console.log( JSON.stringify(pipesModels[i] ) )
    }
  },

  isblockInModelList: function(pipesModels, blockName) {
      console.log("blockInModelList " + blockName)
      var isInList = false
      for (var i = 0; i < pipesModels.length; i++) { 
        console.log("Checking " + pipesModels[i].label)
        if ( pipesModels[i].label == blockName ) {
          isInList = true
          break
        }
      }
      return isInList
    },

  isblockOnGraph(liteGraph, blockName) {
    console.log("blockIsOnGraph " + blockName)
    var isOnGraph = false
    var g = liteGraph.serialize()
    for (var x = 0; x < g.nodes.length; x++) {
      //console.log("Checking " + g.nodes[x].type)
      if ( g.nodes[x].type == blockName ) {
        console.log("Block is on graph: " + JSON.stringify(g.nodes[x]))
        isOnGraph = true
        break
      }
    }
    return isOnGraph
},

// Graphs have duplicate blocks - analyse this
findDuplicateBlocks: function(pipesModels) {
  console.log("findDuplicateBlocks ")
  var checkArray = []
  for (var i = 0; i < pipesModels.length; i++) { 
    if ( pipesModels[i].source == "Sources" || pipesModels[i].source == 'Entities' ) checkArray.push(pipesModels[i])
  }

  for (var i = 0; i < checkArray.length; i++) { 
    var block = checkArray[i]
    for (var x = ++i; x < checkArray.length; x++) { 
      if ( checkArray[x].label == block.label && checkArray[x].sources == block.sources  )
     // console.log("DUPLICATE: " + block.label + "/" + block.sources)
      if ( checkArray[x].fields.length == block.fields.length && checkArray[x].fields == block.fields ) {
    //    console.log("DUPLICATE NUMBER OF FIELDS")
      }
    }
  }

}

}

}

export default LiteGraphHelper
