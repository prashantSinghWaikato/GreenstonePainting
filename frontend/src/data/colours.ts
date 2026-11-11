export type PaletteColour = {
  id: string
  name: string
  hex: string
  reseneUrl: string | null
  displayOrder: number
  defaultInterior: boolean
  defaultExterior: boolean
}

export const paletteColours: PaletteColour[] = [
  { id: 'sea-fog', name: 'Sea Fog', hex: '#e9e7e3', reseneUrl: null, displayOrder: 1, defaultInterior: true, defaultExterior: false },
  { id: 'thorndon-cream', name: 'Thorndon Cream', hex: '#dcd7c6', reseneUrl: null, displayOrder: 2, defaultInterior: false, defaultExterior: false },
  { id: 'white-pointer', name: 'White Pointer', hex: '#e1ddd7', reseneUrl: null, displayOrder: 3, defaultInterior: false, defaultExterior: false },
  { id: 'black-white', name: 'Black White', hex: '#ebe9e5', reseneUrl: null, displayOrder: 4, defaultInterior: false, defaultExterior: false },
  { id: 'lemon-grass', name: 'Lemon Grass', hex: '#999a86', reseneUrl: null, displayOrder: 5, defaultInterior: false, defaultExterior: true },
  { id: 'xanadu', name: 'Xanadu', hex: '#75876e', reseneUrl: null, displayOrder: 6, defaultInterior: false, defaultExterior: false },
  { id: 'patina', name: 'Patina', hex: '#639283', reseneUrl: null, displayOrder: 7, defaultInterior: false, defaultExterior: false },
  { id: 'stonewall', name: 'Stonewall', hex: '#807661', reseneUrl: null, displayOrder: 8, defaultInterior: false, defaultExterior: false },
  { id: 'west-coast', name: 'West Coast', hex: '#5c512f', reseneUrl: null, displayOrder: 9, defaultInterior: false, defaultExterior: false },
  { id: 'green-leaf', name: 'Green Leaf', hex: '#526b2d', reseneUrl: null, displayOrder: 10, defaultInterior: false, defaultExterior: false },
]
